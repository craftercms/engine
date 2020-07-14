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

import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.search.elasticsearch.impl.AbstractElasticsearchWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.LocaleUtils.localeLookupList;
import static org.craftercms.commons.locale.LocaleUtils.CONFIG_KEY_DEFAULT_LOCALE;
import static org.craftercms.commons.locale.LocaleUtils.CONFIG_KEY_FALLBACK;
import static org.craftercms.commons.locale.LocaleUtils.appendLocale;
import static org.craftercms.commons.locale.LocaleUtils.parseLocale;
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

    private static final String DEFAULT_ROLE_FIELD_NAME = "authorizedRoles.item.role";

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

        String aliasName = String.format(indexIdFormat, siteContext.getSiteName());

        // list of aliases to query
        List<String> aliases = new LinkedList<>();

        if (siteContext.isTranslationEnabled()) {
            boolean fallbackToDefault = siteContext.getTranslationConfig().getBoolean(CONFIG_KEY_FALLBACK);
            Locale defaultLocale = parseLocale(siteContext.getTranslationConfig().getString(CONFIG_KEY_DEFAULT_LOCALE));
            Locale currentLocale = LocaleContextHolder.getLocaleContext().getLocale();

            if (currentLocale != null && !currentLocale.equals(defaultLocale)) {
                // if the current locale is other than the default, include the alias for it
                localeLookupList(currentLocale).forEach(locale -> aliases.add(appendLocale(aliasName, locale)));
            }

            if (fallbackToDefault && defaultLocale != null) {
                // if fallback is enabled, add the default locale
                localeLookupList(defaultLocale).forEach(locale -> aliases.add(appendLocale(aliasName, locale)));
            }
        }

        // the original alias will always be included for backward compatibility
        aliases.add(aliasName);

        logger.debug("Executing query for aliases: {}", aliases);

        // Override the indices field in the request
        request.indices(aliases.toArray(new String[0]));

        if (aliases.size() > 1) {
            // Boost the results based on the index
            float boost = 1;
            ListIterator<String> iterator = aliases.listIterator(aliases.size());
            while (iterator.hasPrevious()) {
                request.source().indexBoost(iterator.previous(), boost);
                // TODO: Make this value configurable per site
                boost += 0.05;
            }

            // Don't fail if one of the indices doesn't exist
            request.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

            // Fix scores across multiple indices
            request.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        }
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
                            .map(Object::toString)
                            .collect(joining(" "))));
        } else {
            logger.debug("Filtering search to show only public items");
        }

        mainQuery.filter(boolQuery().must(securityQuery));
    }

}
