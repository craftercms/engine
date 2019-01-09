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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.security.authentication.impl.LoginSuccessHandlerImpl;

/**
 * Extension of {@link org.craftercms.security.authentication.impl.LoginSuccessHandlerImpl} that uses site config
 * to override the default properties.
 *
 * @author avasquez
 */
public class ConfigAwareLoginSuccessHandler extends LoginSuccessHandlerImpl {

    public static final String LOGIN_DEFAULT_SUCCESS_URL_KEY = "security.login.defaultSuccessUrl";
    public static final String LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY = "security.login.alwaysUseDefaultSuccessUrl";

    @Override
    protected String getDefaultTargetUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(LOGIN_DEFAULT_SUCCESS_URL_KEY, defaultTargetUrl);
        } else {
            return defaultTargetUrl;
        }
    }

    @Override
    protected boolean isAlwaysUseDefaultTargetUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY, alwaysUseDefaultTargetUrl);
        } else {
            return alwaysUseDefaultTargetUrl;
        }
    }

}
