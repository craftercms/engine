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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.LocaleUtils;
import org.craftercms.search.elasticsearch.impl.AbstractElasticsearchWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.list.SetUniqueList.setUniqueList;
import static org.craftercms.commons.locale.LocaleUtils.appendLocale;
import static org.craftercms.commons.locale.LocaleUtils.getCompatibleLocales;
import static org.craftercms.engine.util.LocaleUtils.getCurrentLocale;
import static org.craftercms.engine.util.LocaleUtils.getDefaultLocale;
import static org.craftercms.engine.util.LocaleUtils.isTranslationEnabled;
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

    private static final String DEFAULT_LOCALES_PARAM_NAME = "locales";

    private static final String DEFAULT_FALLBACK_PARAM_NAME = "localeFallback";

    /**
     * Format used to build the index id
     */
    protected String indexIdFormat;

    protected String roleFieldName = DEFAULT_ROLE_FIELD_NAME;

    protected String localesParameterName = DEFAULT_LOCALES_PARAM_NAME;

    protected String fallbackParameterName = DEFAULT_FALLBACK_PARAM_NAME;

    public SiteAwareElasticsearchService(RestHighLevelClient client, String indexIdFormat) {
        super(client);
        this.indexIdFormat = indexIdFormat;
    }

    public void setRoleFieldName(final String roleFieldName) {
        this.roleFieldName = roleFieldName;
    }

    public void setLocalesParameterName(String localesParameterName) {
        this.localesParameterName = localesParameterName;
    }

    public void setFallbackParameterName(String fallbackParameterName) {
        this.fallbackParameterName = fallbackParameterName;
    }

    protected List<Locale> getLocales() {
        if (!isTranslationEnabled()) {
            return emptyList();
        }
        var locales = setUniqueList(new LinkedList<Locale>());
        var requestContext = RequestContext.getCurrent();
        String useFallback = null;
        if (requestContext != null) {
            var httpRequest = requestContext.getRequest();
            var requestedLocales = httpRequest.getParameter(localesParameterName);
            useFallback = httpRequest.getParameter(fallbackParameterName);
            if (StringUtils.isNotEmpty(requestedLocales)) {
                // split the locales and add all compatible versions to the list
                Stream.of(requestedLocales.split(","))
                        .map(LocaleUtils::parseLocale)
                        .map(LocaleUtils::getCompatibleLocales)
                        .forEach(locales::addAll);
            }
        }
        // if no locales are requested then use the current
        if (locales.isEmpty()) {
            locales.addAll(getCompatibleLocales(getCurrentLocale()));
        }
        // if fallback is requested then include the default locale too
        if (StringUtils.isNotEmpty(useFallback) && Boolean.parseBoolean(useFallback)) {
            locales.addAll(getCompatibleLocales(getDefaultLocale()));
        }
        return locales;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateIndex(final SearchRequest request) {
        var siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            throw new IllegalStateException("Current site context not found");
        }

        var aliasName = String.format(indexIdFormat, siteContext.getSiteName());

        // list of aliases to query
        var aliases = new LinkedList<String>();
        var locales = getLocales();

        if (!locales.isEmpty()) {
            locales.stream()
                    .map(locale -> appendLocale(aliasName, locale))
                    .forEach(aliases::add);
        }

        // the original alias will always be included for backward compatibility
        aliases.add(aliasName);

        logger.debug("Executing query for aliases: {}", aliases);

        // Override the indices field in the request
        request.indices(aliases.toArray(new String[0]));

        if (aliases.size() > 1) {
            // Boost the results based on the index
            var boost = 1f;
            var iterator = aliases.listIterator(aliases.size());
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
