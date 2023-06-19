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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Implementation of {@link ConfigAwareLocaleResolver} that reads the locale from a request header.
 *
 * <p>Supported configuration properties:</p>
 *  <ul>
 *      <li><strong>headerName</strong>: The name of the request header, defaults to
 *      {@code Accept-Language}</li>
 *  </ul>
 *
 * @author joseross
 * @since 4.0.0
 */
public class HeaderLocaleResolver extends ConfigAwareLocaleResolver {

    private static final Logger logger = LoggerFactory.getLogger(HeaderLocaleResolver.class);

    public static final String CONFIG_KEY_HEADER_NAME = "headerName";
    public static final String DEFAULT_HEADER_NAME = HttpHeaders.ACCEPT_LANGUAGE;

    /**
     * The name of the header
     */
    protected String headerName;

    @Override
    protected void init(HierarchicalConfiguration<?> config) {
        headerName = config.getString(CONFIG_KEY_HEADER_NAME, DEFAULT_HEADER_NAME);
    }

    @Override
    protected Locale resolveLocale(SiteContext siteContext, HttpServletRequest request) {
        if (isNotEmpty(request.getHeader(headerName))) {
            Enumeration<Locale> locales = request.getLocales();
            while (locales.hasMoreElements()) {
                Locale locale = locales.nextElement();
                if (isSupported(locale)) {
                    logger.debug("Found supported locale '{}' requested by the client", locale);
                    return locale;
                } else {
                    logger.debug("Locale '{}' requested by the client is not supported, will be skipped", locale);
                }
            }
        } else {
            logger.debug("The request doesn't include a '{}' header, will be skipped", headerName);
        }
        return null;
    }

}
