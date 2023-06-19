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
package org.craftercms.engine.util.spring.servlet.i18n;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.Locale;

import static org.craftercms.commons.locale.LocaleUtils.parseLocale;

/**
 * Implementation of {@link ConfigAwareLocaleResolver} that reads the locale from a cookie.
 *
 * <p>Supported configuration properties:</p>
 *  <ul>
 *      <li><strong>cookieName</strong>: The name of the cookie to read, the site name will be appended, defaults
 *      to a global configurable value</li>
 *  </ul>
 *
 * @author joseross
 * @since 4.0.0
 */
public class CookieLocaleResolver extends ConfigAwareLocaleResolver {

    public static final String CONFIG_KEY_COOKIE_NAME = "cookieName";

    /**
     * The name of the cookie
     */
    protected String cookieName;

    /**
     * The default name of the cookie, provided for backward compatibility
     */
    protected String defaultCookieName;

    @ConstructorProperties({"defaultCookieName"})
    public CookieLocaleResolver(String defaultCookieName) {
        this.defaultCookieName = defaultCookieName;
    }

    @Override
    protected void init(HierarchicalConfiguration<?> config) {
        cookieName = config.getString(CONFIG_KEY_COOKIE_NAME, defaultCookieName);
    }

    @Override
    protected Locale resolveLocale(SiteContext siteContext, HttpServletRequest request) {
        String actualCookieName = String.format("%s-%s", cookieName, siteContext.getSiteName());
        Cookie cookie = WebUtils.getCookie(request, actualCookieName);
        if (cookie != null) {
            String localeValue = cookie.getValue();
            logger.debug("Using locale '{}' from cookie '{}'", localeValue, actualCookieName);
            return parseLocale(localeValue);
        } else {
            logger.debug("Cookie '{}' not found, will be skipped", actualCookieName);
        }
        return null;
    }
}
