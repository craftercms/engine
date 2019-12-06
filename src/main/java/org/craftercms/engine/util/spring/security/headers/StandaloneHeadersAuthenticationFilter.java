/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.engine.util.spring.security.headers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.engine.util.spring.security.CustomUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Implementation of {@link AbstractHeadersAuthenticationFilter} for standalone authentication
 *
 * @author joseross
 * @since 3.1.5
 */
public class StandaloneHeadersAuthenticationFilter extends AbstractHeadersAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneHeadersAuthenticationFilter.class);

    public static final String STANDALONE_CONFIG_KEY = HEADERS_CONFIG_KEY + ".standalone";

    protected CacheTemplate cacheTemplate;

    public StandaloneHeadersAuthenticationFilter(final CacheTemplate cacheTemplate) {
        super(STANDALONE_CONFIG_KEY);
        setSupportedPrincipalClass(CustomUser.class);

        this.cacheTemplate = cacheTemplate;
    }

    @Override
    protected Object doGetPreAuthenticatedPrincipal(final HttpServletRequest request) {
        String username = request.getHeader(usernameHeaderName);

        if (isNotEmpty(username)) {
            HierarchicalConfiguration siteConfig = ConfigUtils.getCurrentConfig();
            Collection<GrantedAuthority> authorities = getAuthorities(request, siteConfig);
            CustomUser user = new CustomUser(username, username, authorities);
            addAttributes(user, request, siteConfig);
            return user;
        }

        return null;
    }

    /**
     * Gets the roles based on the requests headers, applying and optional mapping based on the site configuration
     */
    @SuppressWarnings("unchecked")
    protected Collection<GrantedAuthority> getAuthorities(final HttpServletRequest request,
                                                          final HierarchicalConfiguration config) {
        String groups = request.getHeader(groupsHeaderName);
        if (StringUtils.isNotEmpty(groups)) {
            Map<String, String> roleMapping = cacheTemplate.getObject(SiteContext.getCurrent().getContext(), () -> {
                List<HierarchicalConfiguration> groupsConfig = config.childConfigurationsAt(HEADERS_GROUPS_CONFIG_KEY);
                if(CollectionUtils.isNotEmpty(groupsConfig)) {
                    return groupsConfig.stream().collect(Collectors.toMap(
                        groupConfig -> groupConfig.getString(NAME_CONFIG_KEY),
                        groupConfig -> groupConfig.getString(ROLE_CONFIG_KEY)));
                }
                logger.debug("No groups mapping found in site configuration");
                return emptyMap();
            }, ROLE_CONFIG_KEY);

            return Arrays.stream(groups.split(","))
                .filter(StringUtils::isNotEmpty)
                .map(String::trim)
                .map(group -> roleMapping.getOrDefault(group, group))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        } else {
            logger.debug("Groups header '{}' was not present in the request", groupsHeaderName);
            return emptySet();
        }
    }

    /**
     * Sets additional attributes on the given user based on the requests headers and the site configuration
     */
    @SuppressWarnings("unchecked")
    protected void addAttributes(final CustomUser user, final HttpServletRequest request,
                                 final HierarchicalConfiguration config) {
        List<HierarchicalConfiguration> attrsConfig = config.childConfigurationsAt(HEADERS_ATTRS_CONFIG_KEY);
        if (CollectionUtils.isNotEmpty(attrsConfig)) {
            attrsConfig.forEach(attrConfig -> {
                String headerName = attrConfig.getString(NAME_CONFIG_KEY);
                String fieldName = attrConfig.getString(FIELD_CONFIG_KEY);
                String fieldValue = request.getHeader(headerPrefix + headerName);

                if (isNotEmpty(fieldValue)) {
                    logger.debug("Adding attribute '{}' with value '{}'", fieldName, fieldValue);
                    user.setAttribute(fieldName, fieldValue);
                } else {
                    logger.debug("Expected header '{}' was not present in the request", headerName);
                }
            });
        }
    }

}
