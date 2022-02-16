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

import org.craftercms.engine.service.context.SiteContext;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.ConstructorProperties;
import java.util.Locale;

/**
 * Implementation of {@link LocaleResolver} that delegates the actual work to the {@link ChainLocaleResolver} if
 * available or to a default object. This provides backward compatibility for sites without translation configuration.
 *
 * @author joseross
 * @since 4.0.0
 */
public class DelegatingLocaleResolver implements LocaleResolver {

    /**
     * The default {@link LocaleResolver}
     */
    protected LocaleResolver defaultLocaleResolver;

    @ConstructorProperties({"defaultLocaleResolver"})
    public DelegatingLocaleResolver(LocaleResolver defaultLocaleResolver) {
        this.defaultLocaleResolver = defaultLocaleResolver;
    }

    protected LocaleResolver getDelegate() {
        LocaleResolver resolver = SiteContext.getCurrent().getLocaleResolver();
        return resolver != null? resolver : defaultLocaleResolver;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return getDelegate().resolveLocale(request);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        getDelegate().setLocale(request, response, locale);
    }

}
