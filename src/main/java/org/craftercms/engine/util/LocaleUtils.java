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
package org.craftercms.engine.util;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.configuration2.Configuration;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

/**
 * Extension of {@link org.craftercms.commons.locale.LocaleUtils} that automatically uses the current locale &amp; fallback
 * according to the site configuration
 *
 * @author joseross
 * @since 4.0.0
 */
public abstract class LocaleUtils extends org.craftercms.commons.locale.LocaleUtils {
    
    public static Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    public static boolean isTranslationEnabled() {
        var context = SiteContext.getCurrent();
        if (context != null) {
            return context.isTranslationEnabled();
        }
        return false;
    }

    public static Locale getDefaultLocale() {
        var context = SiteContext.getCurrent();
        if (context != null) {
            return getDefaultLocale(context.getTranslationConfig());
        }
        return null;
    }

    public static Locale getDefaultLocale(Configuration config) {
        if (config != null) {
            return parseLocale(config.getString(CONFIG_KEY_DEFAULT_LOCALE, null));
        }
        return null;
    }

    public static boolean isLocaleFallbackEnabled() {
        var context = SiteContext.getCurrent();
        if (context != null) {
            var config = context.getTranslationConfig();
            if (config != null) {
                return config.getBoolean(CONFIG_KEY_FALLBACK, false);
            }
        }
        return false;
    }

    public static List<Locale> getCompatibleLocales() {
        var currentLocale = LocaleContextHolder.getLocale();
        var defaultLocale = isLocaleFallbackEnabled()? getDefaultLocale() : null;
        return getCompatibleLocales(currentLocale, defaultLocale);
    }

    public static String resolveLocalePath(String path, Predicate<String> exists) {
        var currentLocale = LocaleContextHolder.getLocale();
        var defaultLocale = isLocaleFallbackEnabled()? getDefaultLocale() : null;
        return findPath(path, currentLocale, defaultLocale, exists);
    }

    public static List<Locale> getSupportedLocales() {
        var context = SiteContext.getCurrent();
        if (context != null) {
            return getSupportedLocales(context.getTranslationConfig());
        }
        return emptyList();
    }

    public static List<Locale> getSupportedLocales(Configuration config) {
        if (config != null) {
            return parseLocales(config.getList(String.class, CONFIG_KEY_SUPPORTED_LOCALES)).stream()
                    .map(locale -> getCompatibleLocales(locale, null))
                    .reduce(new LinkedList<>(), ListUtils::union);
        }
        return emptyList();
    }

}
