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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.craftercms.commons.locale.LocaleUtils.parseLocale;

/**
 * Implementation of {@link ConfigAwareLocaleResolver} that compares the requested URL to a map of locales
 *
 * <p>Supported configuration properties:</p>
 * <ul>
 *     <li><strong>mappings.mapping</strong>: A list of objects containing:<ul>
 *         <li><strong>pattern</strong>: A regular expression to match against the full URL of the request</li>
 *         <li><strong>localeCode</strong>: The code of the locale to use</li>
 *     </ul></li>
 * </ul>
 *
 * @author joseross
 * @since 4.0.0
 */
public class UrlPatternLocaleResolver extends ConfigAwareLocaleResolver {

    public static final String CONFIG_KEY_MAPPINGS = "mappings.mapping";
    public static final String CONFIG_KEY_PATTERN = "pattern";
    public static final String CONFIG_KEY_LOCALE = "localeCode";

    /**
     * The map of URL patterns and locales
     */
    protected Map<String, Locale> localeMapping = new HashMap<>();

    @Override
    protected void init(HierarchicalConfiguration<?> config) {
        config.configurationsAt(CONFIG_KEY_MAPPINGS).forEach(mappingConf ->
            localeMapping.put(mappingConf.getString(CONFIG_KEY_PATTERN),
                    parseLocale(mappingConf.getString(CONFIG_KEY_LOCALE)))
        );
    }

    @Override
    protected Locale resolveLocale(SiteContext siteContext, HttpServletRequest request) {
        logger.debug("Looking match for URL: {}", request.getRequestURL());
        return localeMapping.entrySet().stream()
                .filter(entry -> request.getRequestURL().toString().matches(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

}
