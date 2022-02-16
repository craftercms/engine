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

package org.craftercms.engine.util.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Extension of {@link SavedRequestAwareAuthenticationSuccessHandler} that uses site config to override properties
 *
 * @author joseross
 * @since 3.1.5
 */
public class ConfigAwareAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String LOGIN_DEFAULT_SUCCESS_URL_KEY = "security.login.defaultSuccessUrl";
    public static final String LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY = "security.login.alwaysUseDefaultSuccessUrl";

    @Override
    protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response) {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(LOGIN_DEFAULT_SUCCESS_URL_KEY, super.determineTargetUrl(request, response));
        }
        return super.determineTargetUrl(request, response);
    }

    @Override
    protected boolean isAlwaysUseDefaultTargetUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY, super.isAlwaysUseDefaultTargetUrl());
        }
        return super.isAlwaysUseDefaultTargetUrl();
    }

}
