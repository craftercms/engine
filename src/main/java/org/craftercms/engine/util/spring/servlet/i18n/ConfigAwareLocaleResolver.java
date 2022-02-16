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
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Base implementation for {@link LocaleResolver} by the {@link ChainLocaleResolver}.
 *
 * <p>Resolvers extending this class can be customized using configuration properties and should never implement
 * the {@code setLocale} method because the {@link ChainLocaleResolver} handles that.</p>
 *
 * @author joseross
 * @since 4.0.0
 */
public abstract class ConfigAwareLocaleResolver implements LocaleResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The supported locales
     */
    private List<Locale> supportedLocales;

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    protected boolean isSupported(Locale locale) {
        return isEmpty(supportedLocales) || supportedLocales.contains(locale);
    }

    /**
     * Performs any customization needed
     *
     * @param config the configuration
     */
    protected abstract void init(HierarchicalConfiguration<?> config);

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            throw new IllegalStateException("Site context could not be resolved");
        }

        return resolveLocale(siteContext, request);
    }

    /**
     * Performs the actual work to resolve a locale
     *
     * @param siteContext the current site context
     * @param request the current request
     * @return a locale object or null
     */
    protected abstract Locale resolveLocale(SiteContext siteContext, HttpServletRequest request);

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("This class in unable to store locales");
    }

}
