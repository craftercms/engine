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

package org.craftercms.engine.util.spring.security.headers;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.engine.util.spring.security.ConfigAwarePreAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;

/**
 * Extension of {@link ConfigAwarePreAuthenticationFilter} for handling header based pre-authentication
 *
 * @author joseross
 * @since 3.1.5
 */
public abstract class AbstractHeadersAuthenticationFilter extends ConfigAwarePreAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHeadersAuthenticationFilter.class);

    public static final String DEFAULT_HEADER_PREFIX = "MELLON_";
    public static final String DEFAULT_USERNAME_HEADER_NAME = DEFAULT_HEADER_PREFIX + "username";
    public static final String DEFAULT_EMAIL_HEADER_NAME = DEFAULT_HEADER_PREFIX + "email";
    public static final String DEFAULT_GROUPS_HEADER_NAME = DEFAULT_HEADER_PREFIX + "groups";
    public static final String DEFAULT_TOKEN_HEADER_NAME = DEFAULT_HEADER_PREFIX + "secure_key";

    public static final String HEADERS_CONFIG_KEY = "security.headers";
    public static final String HEADERS_TOKEN_CONFIG_KEY = HEADERS_CONFIG_KEY + ".token";
    public static final String HEADERS_ATTRS_CONFIG_KEY = HEADERS_CONFIG_KEY + ".attributes";
    public static final String HEADERS_GROUPS_CONFIG_KEY = HEADERS_CONFIG_KEY + ".groups";

    public static final String NAME_CONFIG_KEY = "name";
    public static final String FIELD_CONFIG_KEY = "field";
    public static final String ROLE_CONFIG_KEY = "role";

    protected String headerPrefix = DEFAULT_HEADER_PREFIX;
    protected String usernameHeaderName = DEFAULT_USERNAME_HEADER_NAME;
    protected String emailHeaderName = DEFAULT_EMAIL_HEADER_NAME;
    protected String groupsHeaderName = DEFAULT_GROUPS_HEADER_NAME;
    protected String tokenHeaderName = DEFAULT_TOKEN_HEADER_NAME;
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
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null && config.containsKey(HEADERS_TOKEN_CONFIG_KEY)) {
            return config.getString(HEADERS_TOKEN_CONFIG_KEY);
        }
        return defaultTokenValue;
    }

    protected boolean hasValidToken(HttpServletRequest request) {
        logger.debug("Checking security token from request headers");
        String tokenHeaderValue = request.getHeader(tokenHeaderName);
        if (StringUtils.isEmpty(tokenHeaderValue)) {
            logger.debug("No security token found for request from '{}'", request.getRemoteAddr());
            return false;
        } else if (!StringUtils.equals(tokenHeaderValue, getTokenExpectedValue())) {
            logger.warn("Security token mismatch during authentication from '{}'", request.getRemoteAddr());
            return false;
        }
        return true;
    }

}
