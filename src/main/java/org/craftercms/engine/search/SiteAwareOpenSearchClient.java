/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.LocaleUtils;
import org.craftercms.search.opensearch.impl.client.AbstractOpenSearchClientWrapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SearchType;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.list.SetUniqueList.setUniqueList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.commons.locale.LocaleUtils.appendLocale;
import static org.craftercms.commons.locale.LocaleUtils.getCompatibleLocales;
import static org.craftercms.engine.util.LocaleUtils.*;

/**
 * Implementation of {@link AbstractOpenSearchClientWrapper} that sets the index and security filters based on the
 * current site context for all search requests.
 *
 * @author joseross
 * @since 4.0.0
 */
public class SiteAwareOpenSearchClient extends AbstractOpenSearchClientWrapper {

    private static final String DEFAULT_ROLE_FIELD_NAME = "authorizedRoles.item.role";

    private static final String DEFAULT_LOCALES_PARAM_NAME = "locales";

    private static final String DEFAULT_FALLBACK_PARAM_NAME = "localeFallback";

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Format used to build the index id
     */
    protected String indexIdFormat;

    protected String roleFieldName = DEFAULT_ROLE_FIELD_NAME;

    protected String localesParameterName = DEFAULT_LOCALES_PARAM_NAME;

    protected String fallbackParameterName = DEFAULT_FALLBACK_PARAM_NAME;

    protected final boolean enableTranslation;

    @ConstructorProperties({"client", "indexIdFormat", "enableTranslation"})
    public SiteAwareOpenSearchClient(OpenSearchClient client, String indexIdFormat, boolean enableTranslation) {
        super(client);
        this.indexIdFormat = indexIdFormat;
        this.enableTranslation = enableTranslation;
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
        if (!(enableTranslation && isTranslationEnabled())) {
            return emptyList();
        }
        SetUniqueList<Locale> locales = setUniqueList(new LinkedList<>());
        RequestContext requestContext = RequestContext.getCurrent();
        String useFallback = null;
        if (requestContext != null) {
            HttpServletRequest httpRequest = requestContext.getRequest();
            String requestedLocales = httpRequest.getParameter(localesParameterName);
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

    protected String addPrefix(SiteContext siteContext, String name) {
        return String.format("%s_%s", siteContext.getSiteName(), name);
    }

    @Override
    protected void updateIndex(SearchRequest request, Map<String, Object> parameters, RequestUpdates updates) {
        // Call the parent to support custom index search
        super.updateIndex(request, parameters, updates);

        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            throw new IllegalStateException("Current site context not found");
        }

        // Generate the default alias for the current site
        String aliasName = String.format(indexIdFormat, siteContext.getSiteName());
        // Get the requested indices
        List<String> currentIndices = updates.getIndex();

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

        List<Map<String, Double>> boosting = new LinkedList<>();

        if (isNotEmpty(currentIndices)) {
            // Add the site name prefix for all indices
            currentIndices.stream().map(index -> addPrefix(siteContext, index)).forEach(aliases::add);

            // Add the site name prefix for the boosting if needed
            List<Map<String, Double>> existingBoosting = request.indicesBoost();
            if (isNotEmpty(existingBoosting)) {
                existingBoosting.stream()
                                .map(boost -> boost.entrySet().stream()
                                        .collect(toMap(entry -> addPrefix(siteContext, entry.getKey()),
                                                       Map.Entry::getValue)))
                                .forEach(boosting::add);
            }
        }

        logger.debug("Executing query for aliases: {}", aliases);

        // Override the indices field in the request
        updates.setIndex(aliases);

        if (aliases.size() > 1) {
            // Boost the results based on the index
            var boost = 1d;
            var iterator = aliases.listIterator(aliases.size());
            while (iterator.hasPrevious()) {
                boosting.add(Map.of(iterator.previous(), boost));
                // TODO: Make this value configurable per site
                boost += 0.05;
            }
            updates.setIndicesBoost(boosting);

            // Don't fail if one of the indices doesn't exist
            updates.setIgnoreUnavailable(true);

            // Fix scores across multiple indices
            updates.setSearchType(SearchType.DfsQueryThenFetch);
        }
    }

    @Override
    protected void updateQuery(SearchRequest request, Map<String, Object> parameters, RequestUpdates updates) {
        super.updateQuery(request, parameters, updates);

        // Use the updated if it exists
        BoolQuery mainQuery = Optional.ofNullable(updates.getQuery()).orElse(request.query()).bool();

        Authentication auth = SecurityContextHolder.getContext() != null?
                                SecurityContextHolder.getContext().getAuthentication() : null;

        // Include all public items
        BoolQuery.Builder securityQuery = new BoolQuery.Builder()
            .should(s -> s
                .bool(b -> b
                    .mustNot(n -> n
                        .exists(e -> e
                            .field(roleFieldName)
                        )
                    )
                )
            )
            .should(s -> s
                .match(m -> m
                    .field(roleFieldName)
                    .query(q -> q
                        .stringValue("anonymous")
                    )
                )
            );

        if (auth != null && !(auth instanceof AnonymousAuthenticationToken) && isNotEmpty(auth.getAuthorities())) {
            logger.debug("Filtering search results for roles: {}", auth.getAuthorities());
            securityQuery.should(s -> s
                .match(m -> m
                    .field(roleFieldName)
                    .query(q -> q
                        .stringValue(auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .map(role -> role +  StringUtils.SPACE +
                                (startsWith(role, ROLE_PREFIX)? removeStart(role, ROLE_PREFIX)
                                                                : appendIfMissing(role, ROLE_PREFIX)))
                            .collect(joining(StringUtils.SPACE)))
                    )
                )
            );
        } else {
            logger.debug("Filtering search to show only public items");
        }
        updates.setQuery(q -> q
            .bool(b -> b
                .must(mainQuery._toQuery())
                .filter(securityQuery.build()._toQuery())
            )
        );
    }

}
