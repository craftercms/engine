/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.engine.search;

import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.search.elasticsearch.impl.AbstractElasticsearchWrapper;
import org.craftercms.engine.service.context.SiteContext;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

/**
 * Implementation of {@link org.craftercms.search.elasticsearch.ElasticsearchWrapper}
 * that sets the index based on the current site context for all search requests.
 * @author joseross
 * @since 3.1
 */
public class SiteAwareElasticsearchService extends AbstractElasticsearchWrapper {

    private static final Logger logger = LoggerFactory.getLogger(SiteAwareElasticsearchService.class);

    private static final String DEFAULT_ROLE_FIELD_NAME = "authorizedRoles.item.role";

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Format used to build the index id
     */
    protected String indexIdFormat;

    protected String roleFieldName = DEFAULT_ROLE_FIELD_NAME;

    public SiteAwareElasticsearchService(RestHighLevelClient client, String indexIdFormat) {
        super(client);
        this.indexIdFormat = indexIdFormat;
    }

    public void setRoleFieldName(final String roleFieldName) {
        this.roleFieldName = roleFieldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateIndex(final SearchRequest request) {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            throw new IllegalStateException("Current site context not found");
        }

        // Generate the default alias for the current site
        String aliasName = String.format(indexIdFormat, siteContext.getSiteName());
        // Get the requested indices
        String[] currentIndices = request.indices();
        String[] updatedIndices;

        if (ArrayUtils.isNotEmpty(currentIndices)) {
            updatedIndices = new String[currentIndices.length + 1];

            // Add the site name prefix for all indices
            for(int i = 0; i < currentIndices.length; i++) {
                updatedIndices[i] = addPrefix(siteContext, currentIndices[i]);
            }

            // Also add the default index
            updatedIndices[currentIndices.length] = aliasName;

            // Add the site name prefix for the boosting if needed
            List<SearchSourceBuilder.IndexBoost> indexBoosts = new ArrayList<>(request.source().indexBoosts());
            if (isNotEmpty(indexBoosts)) {
                indexBoosts.forEach(indexBoost ->
                        request.source().indexBoost(addPrefix(siteContext, indexBoost.getIndex()),
                                                    indexBoost.getBoost()));

                // Prevent missing index errors, this is needed because the original index boost can't be removed
                IndicesOptions originalOptions = request.indicesOptions();
                request.indicesOptions(IndicesOptions.fromOptions(true, originalOptions.allowNoIndices(),
                        originalOptions.expandWildcardsOpen(), originalOptions.expandWildcardsClosed(),
                        originalOptions.allowAliasesToMultipleIndices(), originalOptions.forbidClosedIndices(),
                        originalOptions.ignoreAliases(), originalOptions.ignoreThrottled()));
            }
        } else {
            // Only query the default index
            updatedIndices = new String[] { aliasName };
        }

        request.indices(updatedIndices);
    }

    protected String addPrefix(SiteContext siteContext, String name) {
        return String.format("%s_%s", siteContext.getSiteName(), name);
    }

    @Override
    protected void updateFilters(final SearchRequest request) {
        super.updateFilters(request);

        BoolQueryBuilder mainQuery = (BoolQueryBuilder) request.source().query();

        Authentication auth = null;
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            auth = context.getAuthentication();
        }

        // Include all public items
        BoolQueryBuilder securityQuery = boolQuery()
                .should(boolQuery().mustNot(existsQuery(roleFieldName)))
                .should(matchQuery(roleFieldName, "anonymous"));

        if (auth != null && !(auth instanceof AnonymousAuthenticationToken) && isNotEmpty(auth.getAuthorities())) {
            logger.debug("Filtering search results for roles: {}", auth.getAuthorities());
            securityQuery.should(matchQuery(roleFieldName, auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .map(role -> role +  " " +
                                    (startsWith(role, ROLE_PREFIX)? removeStart(role, ROLE_PREFIX)
                                            : appendIfMissing(role, ROLE_PREFIX)))
                            .collect(joining(" "))));
        } else {
            logger.debug("Filtering search to show only public items");
        }

        mainQuery.filter(boolQuery().must(securityQuery));
    }

}
