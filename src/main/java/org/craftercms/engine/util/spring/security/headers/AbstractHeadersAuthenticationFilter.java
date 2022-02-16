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

package org.craftercms.engine.util.spring.security.headers;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.engine.util.spring.security.ConfigAwarePreAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * Extension of {@link ConfigAwarePreAuthenticationFilter} for handling header based pre-authentication
 *
 * @author joseross
 * @since 3.1.5
 */
public abstract class AbstractHeadersAuthenticationFilter extends ConfigAwarePreAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHeadersAuthenticationFilter.class);

    public static final String HEADERS_CONFIG_KEY = "security.headers";
    public static final String HEADERS_TOKEN_CONFIG_KEY = HEADERS_CONFIG_KEY + ".token";
    public static final String HEADERS_ATTRS_CONFIG_KEY = HEADERS_CONFIG_KEY + ".attributes";
    public static final String HEADERS_GROUPS_CONFIG_KEY = HEADERS_CONFIG_KEY + ".groups";

    public static final String HEADER_NAME_CONFIG_KEY = HEADERS_CONFIG_KEY + ".names";
    public static final String HEADERS_NAME_PREFIX_CONFIG_KEY = HEADER_NAME_CONFIG_KEY + ".prefix";
    public static final String HEADERS_NAME_USERNAME_CONFIG_KEY = HEADER_NAME_CONFIG_KEY + ".username";
    public static final String HEADERS_NAME_EMAIL_CONFIG_KEY = HEADER_NAME_CONFIG_KEY + ".email";
    public static final String HEADERS_NAME_GROUPS_CONFIG_KEY = HEADER_NAME_CONFIG_KEY + ".groups";
    public static final String HEADERS_NAME_TOKEN_CONFIG_KEY = HEADER_NAME_CONFIG_KEY + ".token";


    public static final String NAME_CONFIG_KEY = "name";
    public static final String FIELD_CONFIG_KEY = "field";
    public static final String ROLE_CONFIG_KEY = "role";

    private String headerPrefix;
    private String usernameHeaderName;
    private String emailHeaderName;
    private String groupsHeaderName;
    private String tokenHeaderName;
    protected String defaultTokenValue;

    public AbstractHeadersAuthenticationFilter(String enabledConfigKey) {
        super(enabledConfigKey);
        setCheckForPrincipalChanges(true);
    }

    public void setHeaderPrefix(final String headerPrefix) {
        this.headerPrefix = headerPrefix;
    }

    public void setUsernameHeaderName(final String usernameHeaderName) {
        this.usernameHeaderName = usernameHeaderName;
    }

    public void setEmailHeaderName(final String emailHeaderName) {
        this.emailHeaderName = emailHeaderName;
    }

    public void setGroupsHeaderName(final String groupsHeaderName) {
        this.groupsHeaderName = groupsHeaderName;
    }

    public void setTokenHeaderName(final String tokenHeaderName) {
        this.tokenHeaderName = tokenHeaderName;
    }

    public void setDefaultTokenValue(final String defaultTokenValue) {
        this.defaultTokenValue = defaultTokenValue;
    }

    public String getHeaderPrefix() {
        return getConfigProperty(HEADERS_NAME_PREFIX_CONFIG_KEY, headerPrefix);
    }

    public String getUsernameHeaderName() {
        return getHeaderPrefix() + getConfigProperty(HEADERS_NAME_USERNAME_CONFIG_KEY, usernameHeaderName);
    }

    public String getEmailHeaderName() {
        return getHeaderPrefix() + getConfigProperty(HEADERS_NAME_EMAIL_CONFIG_KEY, emailHeaderName);
    }

    public String getGroupsHeaderName() {
        return getHeaderPrefix() + getConfigProperty(HEADERS_NAME_GROUPS_CONFIG_KEY, groupsHeaderName);
    }

    public String getTokenHeaderName() {
        return getHeaderPrefix() + getConfigProperty(HEADERS_NAME_TOKEN_CONFIG_KEY, tokenHeaderName);
    }

    protected abstract Object doGetPreAuthenticatedPrincipal(final HttpServletRequest request);

    @Override
    protected boolean principalChanged(final HttpServletRequest request, final Authentication currentAuthentication) {
        if (hasValidToken(request)) {
            return super.principalChanged(request, currentAuthentication);
        }
        return false;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        if (hasValidToken(request)) {
            return doGetPreAuthenticatedPrincipal(request);
        }
        return null;
    }

    protected String getTokenExpectedValue() {
        return getConfigProperty(HEADERS_TOKEN_CONFIG_KEY, defaultTokenValue);
    }

    protected boolean hasValidToken(HttpServletRequest request) {
        logger.debug("Checking security token from request headers");
        String tokenHeaderValue = request.getHeader(getTokenHeaderName());
        if (StringUtils.isEmpty(tokenHeaderValue)) {
            logger.debug("No security token found for request from '{}'", request.getRemoteAddr());
            return false;
        } else if (!StringUtils.equals(tokenHeaderValue, getTokenExpectedValue())) {
            logger.warn("Security token mismatch during authentication from '{}'", request.getRemoteAddr());
            return false;
        }
        return true;
    }

    protected String getConfigProperty(String key, String defaultValue) {
        HierarchicalConfiguration<?> config = ConfigUtils.getCurrentConfig();
        if (config != null && config.containsKey(key)) {
            return config.getString(key);
        }
        return defaultValue;
    }

}
