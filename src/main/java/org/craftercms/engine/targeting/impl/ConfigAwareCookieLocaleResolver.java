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
package org.craftercms.engine.targeting.impl;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.LocaleUtils;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/**
 * {@link CookieLocaleResolver} extension that uses the default locale specified in the site configuration if
 * the user has not current locale associated.
 *
 * @author avasquez
 */
public class ConfigAwareCookieLocaleResolver extends CookieLocaleResolver {

    public static final String DEFAULT_LOCALE_CONFIG_KEY = "defaultLocale";

    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request) {
        Locale defaultLocale = getDefaultLocaleFromConfig();
        if (defaultLocale != null) {
            return defaultLocale;
        } else {
            return super.determineDefaultLocale(request);
        }
    }

    protected Locale getDefaultLocaleFromConfig() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            Locale defaultLocale = LocaleUtils.toLocale(config.getString(DEFAULT_LOCALE_CONFIG_KEY));
            if (defaultLocale != null && !LocaleUtils.isAvailableLocale(defaultLocale)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(defaultLocale + " is not one of the available locales");
                }

                return null;
            }

            return defaultLocale;
        } else {
            return null;
        }
    }

}
