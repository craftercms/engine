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
import org.craftercms.engine.util.spring.security.CustomUser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.craftercms.commons.locale.LocaleUtils.parseLocale;

/**
 * Implementation of {@link ConfigAwareLocaleResolver} that extracts the locale from the current authenticated user
 *
 * <p>Supported configuration properties:</p>
 *  <ul>
 *      <li><strong>attributeName</strong>: The name of the attribute to use, defaults to {@code preferredLanguage}</li>
 *  </ul>
 *
 * @author joseross
 * @since 4.0.0
 */
public class PrincipalLocaleResolver extends ConfigAwareLocaleResolver {

    public static final String DEFAULT_ATTRIBUTE_NAME = "preferredLanguage";

    public static final String CONFIG_KEY_ATTRIBUTE_NAME = "attributeName";

    protected String attributeName;

    @Override
    protected void init(HierarchicalConfiguration<?> config) {
        attributeName = config.getString(CONFIG_KEY_ATTRIBUTE_NAME, DEFAULT_ATTRIBUTE_NAME);
    }

    @Override
    protected Locale resolveLocale(SiteContext siteContext, HttpServletRequest request) {
        var context = SecurityContextHolder.getContext();
        if (context != null) {
            var authentication = context.getAuthentication();
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                var principal = (CustomUser) authentication.getPrincipal();
                if (principal != null) {
                    return parseLocale(principal.getAttribute(attributeName));
                }
            }
        }
        return null;
    }

}
