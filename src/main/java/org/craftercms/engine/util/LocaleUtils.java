/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.engine.util;

import org.apache.commons.lang.StringUtils;
import org.craftercms.core.util.url.ContentBundleUrl;
import org.craftercms.core.util.url.ContentBundleUrlParser;

import java.util.List;
import java.util.Locale;

/**
 * @author Alfonso Vásquez
 */
public class LocaleUtils {

    /**
     * Calls {@link org.apache.commons.lang.LocaleUtils#localeLookupList(java.util.Locale, java.util.Locale)}
     * with the ROOT locale as the default locale.
     */
    public static List<Locale> getLocaleLookupList(Locale locale) {
        return org.apache.commons.lang.LocaleUtils.localeLookupList(locale, Locale.ROOT);
    }

    /**
     * Returns a new url base on the given locale. The implementation returns an url with (delim is normally '_'):
     * <pre>
     *     baseName + delim + language + delim + country + delim + variant
     * </pre>
     */
    public static String getLocalizedUrl(ContentBundleUrlParser urlParser, String baseUrl, Locale locale, String delim) {
        String language = locale.getLanguage();
	    String country = locale.getCountry();
	    String variant = locale.getVariant();

        if (StringUtils.isEmpty(language) && StringUtils.isEmpty(country) && StringUtils.isEmpty(variant)) {
            return baseUrl;
        }

        ContentBundleUrl parsedUrl = urlParser.getContentBundleUrl(baseUrl);
        String localizedUrl = baseUrl;

        if (StringUtils.isNotEmpty(parsedUrl.getBaseNameAndExtensionToken())) {
            StringBuilder localizedUrlBuilder = new StringBuilder(parsedUrl.getBaseNameAndExtensionToken());

            localizedUrlBuilder.append(delim);
            if (StringUtils.isNotEmpty(variant)) {
                localizedUrlBuilder.append(language).append(delim).append(country).append(delim).append(variant);
            } else if (StringUtils.isNotEmpty(country)) {
                localizedUrlBuilder.append(language).append(delim).append(country);
            } else {
                localizedUrlBuilder.append(language);
            }

            localizedUrlBuilder.insert(0, parsedUrl.getPrefix()).append(parsedUrl.getSuffix());

            localizedUrl = localizedUrlBuilder.toString();
        }

        return localizedUrl;
    }

}
