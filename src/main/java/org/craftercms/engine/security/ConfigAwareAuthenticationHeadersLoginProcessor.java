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

package org.craftercms.engine.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.processors.RequestSecurityProcessorChain;
import org.craftercms.security.processors.impl.AuthenticationHeadersLoginProcessor;
import org.craftercms.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of {@link AuthenticationHeadersLoginProcessor} that uses site config to support SAML authentication
 * without requiring Crafter Profile.
 * @author joseross
 * @since 3.1
 */
public class ConfigAwareAuthenticationHeadersLoginProcessor extends AuthenticationHeadersLoginProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ConfigAwareAuthenticationHeadersLoginProcessor.class);

    public static final String SAML_CONFIG_KEY = "security.saml";
    public static final String SAML_TOKEN_CONFIG_KEY = SAML_CONFIG_KEY + ".token";
    public static final String SAML_ATTRS_CONFIG_KEY = SAML_CONFIG_KEY + ".attributes";
    public static final String SAML_GROUPS_CONFIG_KEY = SAML_CONFIG_KEY + ".groups";

    public static final String NAME_CONFIG_KEY = "name";
    public static final String FIELD_CONFIG_KEY = "field";
    public static final String ROLE_CONFIG_KEY = "role";

    public static final String DEFAULT_GROUPS_HEADER_NAME = DEFAULT_MELLON_HEADER_PREFIX + "groups";

    /**
     * The name of the header containing the list of groups for the user
     */
    protected String groupsHeaderName = DEFAULT_GROUPS_HEADER_NAME;

    public void setGroupsHeaderName(final String groupsHeaderName) {
        this.groupsHeaderName = groupsHeaderName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processRequest(final RequestContext context, final RequestSecurityProcessorChain processorChain)
        throws Exception {
        HttpServletRequest request = context.getRequest();
        Authentication auth = SecurityUtils.getAuthentication(request);

        logger.debug("Checking authentication headers");
        String username = request.getHeader(usernameHeaderName);
        String email = request.getHeader(emailHeaderName);

        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (StringUtils.isNoneEmpty(username, email) && Objects.isNull(auth) && Objects.nonNull(config) &&
            config.containsKey(SAML_TOKEN_CONFIG_KEY) && hasValidToken(request)) {

            logger.debug("Using site specific SAML authentication");

            logger.debug("Creating authentication object for '{}'", username);
            Profile profile = new Profile();
            profile.setUsername(username);
            profile.setEmail(email);
            addAttributes(profile, request, config);
            addRoles(profile, request, config);

            SecurityUtils.setAuthentication(request, new PreAuthenticatedProfile(profile));

            processorChain.processRequest(context);
        } else {
            logger.debug("Using Crafter Profile SAML authentication");
            super.processRequest(context, processorChain);
        }
    }

    /**
     * Sets the roles on the given profile based on the requests headers, applying and optional mapping based
     * on the site configuration
     */
    @SuppressWarnings("unchecked")
    protected void addRoles(final Profile profile, final HttpServletRequest request,
                            final HierarchicalConfiguration config) {
        String groups = request.getHeader(groupsHeaderName);
        if (StringUtils.isNotEmpty(groups)) {
            Map<String, String> roleMapping;
            List<HierarchicalConfiguration> groupsConfig = config.childConfigurationsAt(SAML_GROUPS_CONFIG_KEY);
            if(CollectionUtils.isNotEmpty(groupsConfig)) {
                roleMapping = new HashMap<>();
                groupsConfig.forEach(groupConfig ->
                    roleMapping.put(groupConfig.getString(NAME_CONFIG_KEY), groupConfig.getString(ROLE_CONFIG_KEY)));
            } else {
                logger.debug("No groups mapping found in site configuration");
                roleMapping = Collections.emptyMap();
            }

            profile.setRoles(Arrays.stream(groups.split(","))
                .map(String::trim)
                .map(group -> roleMapping.getOrDefault(group, group))
                .collect(Collectors.toSet()));
        } else {
            logger.debug("Groups header '{}' was not present in the request", groupsHeaderName);
        }
    }

    /**
     * Sets additional attributes on the given profile based on the requests headers and the site configuration
     */
    @SuppressWarnings("unchecked")
    protected void addAttributes(final Profile profile, final HttpServletRequest request,
                                 final HierarchicalConfiguration config) {
        List<HierarchicalConfiguration> attrsConfig = config.childConfigurationsAt(SAML_ATTRS_CONFIG_KEY);
        if (CollectionUtils.isNotEmpty(attrsConfig)) {
            attrsConfig.forEach(attrConfig -> {
                String headerName = attrConfig.getString(NAME_CONFIG_KEY);
                String fieldName = attrConfig.getString(FIELD_CONFIG_KEY);
                String fieldValue = request.getHeader(mellonHeaderPrefix + headerName);

                if (StringUtils.isNotEmpty(fieldValue)) {
                    logger.debug("Adding attribute '{}' with value '{}'", fieldName, fieldValue);
                    profile.setAttribute(fieldName, fieldValue);
                } else {
                    logger.debug("Expected header '{}' was not present in the request", headerName);
                }
            });
        }
    }

    @Override
    public String getTokenExpectedValue() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (Objects.nonNull(config) && config.containsKey(SAML_TOKEN_CONFIG_KEY)) {
            return config.getString(SAML_TOKEN_CONFIG_KEY);
        }
        return null;
    }

}
