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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.beans.ConstructorProperties;

/**
 * Extension of {@link LoginUrlAuthenticationEntryPoint} that uses site config to override properties
 *
 * @author joseross
 * @since 3.1.5
 */
public class ConfigAwareLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public static final String LOGIN_FORM_URL_KEY = "security.login.formUrl";

    @ConstructorProperties({"loginFormUrl"})
    public ConfigAwareLoginUrlAuthenticationEntryPoint(final String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String determineUrlToUseForThisRequest(final HttpServletRequest request,
                                                     final HttpServletResponse response,
                                                     final AuthenticationException exception) {
        HierarchicalConfiguration<?> siteConfig = ConfigUtils.getCurrentConfig();
        if (siteConfig != null && siteConfig.containsKey(LOGIN_FORM_URL_KEY)) {
            return siteConfig.getString(LOGIN_FORM_URL_KEY);
        }
        return super.getLoginFormUrl();
    }

}
