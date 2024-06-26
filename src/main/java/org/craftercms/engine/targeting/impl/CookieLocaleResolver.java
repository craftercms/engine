/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.targeting.impl;

import java.util.Locale;
import java.util.TimeZone;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

/**
 * TODO: review this to remove this class
 * This class is a copied version of `CookieLocaleResolver` 5.3.33
 * to keep using method {@link ConfigAwareCookieLocaleResolver#getCookieName()}.
 * Source: https://github.com/spring-projects/spring-framework/blob/v5.3.33/spring-webmvc/src/main/java/org/springframework/web/servlet/i18n/CookieLocaleResolver.java
 *
 */

/**
 * {@link LocaleResolver} implementation that uses a cookie sent back to the user
 * in case of a custom setting, with a fallback to the configured default locale,
 * the request's {@code Accept-Language} header, or the default locale for the server.
 *
 * <p>This is particularly useful for stateless applications without user sessions.
 * The cookie may optionally contain an associated time zone value as well;
 * alternatively, you may specify a default time zone.
 *
 * <p>Custom controllers can override the user's locale and time zone by calling
 * {@code #setLocale(Context)} on the resolver, e.g. responding to a locale change
 * request. As a more convenient alternative, consider using
 * {@link org.springframework.web.servlet.support.RequestContext#changeLocale}.
 *
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 27.02.2003
 * @see #setDefaultLocale
 * @see #setDefaultTimeZone
 */
public class CookieLocaleResolver extends CookieGenerator implements LocaleContextResolver {

    /**
     * The name of the request attribute that holds the {@code Locale}.
     * <p>Only used for overriding a cookie value if the locale has been
     * changed in the course of the current request!
     * <p>Use {@code RequestContext(Utils).getLocale()}
     * to retrieve the current locale in controllers or views.
     * @see org.springframework.web.servlet.support.RequestContext#getLocale
     * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
     */
    public static final String LOCALE_REQUEST_ATTRIBUTE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";

    /**
     * The name of the request attribute that holds the {@code TimeZone}.
     * <p>Only used for overriding a cookie value if the locale has been
     * changed in the course of the current request!
     * <p>Use {@code RequestContext(Utils).getTimeZone()}
     * to retrieve the current time zone in controllers or views.
     * @see org.springframework.web.servlet.support.RequestContext#getTimeZone
     * @see org.springframework.web.servlet.support.RequestContextUtils#getTimeZone
     */
    public static final String TIME_ZONE_REQUEST_ATTRIBUTE_NAME = CookieLocaleResolver.class.getName() + ".TIME_ZONE";

    /**
     * The default cookie name used if none is explicitly set.
     */
    public static final String DEFAULT_COOKIE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";


    private boolean languageTagCompliant = true;

    private boolean rejectInvalidCookies = true;

    @Nullable
    private Locale defaultLocale;

    @Nullable
    private TimeZone defaultTimeZone;


    /**
     * Create a new instance of {@link CookieLocaleResolver} using the
     * {@linkplain #DEFAULT_COOKIE_NAME default cookie name}.
     */
    public CookieLocaleResolver() {
        setCookieName(DEFAULT_COOKIE_NAME);
    }


    /**
     * Specify whether this resolver's cookies should be compliant with BCP 47
     * language tags instead of Java's legacy locale specification format.
     * <p>The default is {@code true}, as of 5.1. Switch this to {@code false}
     * for rendering Java's legacy locale specification format. For parsing,
     * this resolver leniently accepts the legacy {@link Locale#toString}
     * format as well as BCP 47 language tags in any case.
     * @since 4.3
     * @see #parseLocaleValue(String)
     * @see #toLocaleValue(Locale)
     * @see Locale#forLanguageTag(String)
     * @see Locale#toLanguageTag()
     */
    public void setLanguageTagCompliant(boolean languageTagCompliant) {
        this.languageTagCompliant = languageTagCompliant;
    }

    /**
     * Return whether this resolver's cookies should be compliant with BCP 47
     * language tags instead of Java's legacy locale specification format.
     * @since 4.3
     */
    public boolean isLanguageTagCompliant() {
        return this.languageTagCompliant;
    }

    /**
     * Specify whether to reject cookies with invalid content (e.g. invalid format).
     * <p>The default is {@code true}. Turn this off for lenient handling of parse
     * failures, falling back to the default locale and time zone in such a case.
     * @since 5.1.7
     * @see #setDefaultLocale
     * @see #setDefaultTimeZone
     * @see #determineDefaultLocale
     * @see #determineDefaultTimeZone
     */
    public void setRejectInvalidCookies(boolean rejectInvalidCookies) {
        this.rejectInvalidCookies = rejectInvalidCookies;
    }

    /**
     * Return whether to reject cookies with invalid content (e.g. invalid format).
     * @since 5.1.7
     */
    public boolean isRejectInvalidCookies() {
        return this.rejectInvalidCookies;
    }

    /**
     * Set a fixed locale that this resolver will return if no cookie is found.
     */
    public void setDefaultLocale(@Nullable Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Return the fixed locale that this resolver will return if no cookie is found,
     * if any.
     */
    @Nullable
    protected Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    /**
     * Set a fixed time zone that this resolver will return if no cookie is found.
     * @since 4.0
     */
    public void setDefaultTimeZone(@Nullable TimeZone defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    /**
     * Return the fixed time zone that this resolver will return if no cookie is found,
     * if any.
     * @since 4.0
     */
    @Nullable
    protected TimeZone getDefaultTimeZone() {
        return this.defaultTimeZone;
    }


    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        parseLocaleCookieIfNecessary(request);
        return (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
    }

    @Override
    public LocaleContext resolveLocaleContext(final HttpServletRequest request) {
        parseLocaleCookieIfNecessary(request);
        return new TimeZoneAwareLocaleContext() {
            @Override
            @Nullable
            public Locale getLocale() {
                return (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
            }
            @Override
            @Nullable
            public TimeZone getTimeZone() {
                return (TimeZone) request.getAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
            }
        };
    }

    private void parseLocaleCookieIfNecessary(HttpServletRequest request) {
        if (request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) == null) {
            Locale locale = null;
            TimeZone timeZone = null;

            // Retrieve and parse cookie value.
            String cookieName = getCookieName();
            if (cookieName != null) {
                Cookie cookie = WebUtils.getCookie(request, cookieName);
                if (cookie != null) {
                    String value = cookie.getValue();
                    String localePart = value;
                    String timeZonePart = null;
                    int separatorIndex = localePart.indexOf('/');
                    if (separatorIndex == -1) {
                        // Leniently accept older cookies separated by a space...
                        separatorIndex = localePart.indexOf(' ');
                    }
                    if (separatorIndex >= 0) {
                        localePart = value.substring(0, separatorIndex);
                        timeZonePart = value.substring(separatorIndex + 1);
                    }
                    try {
                        locale = (!"-".equals(localePart) ? parseLocaleValue(localePart) : null);
                        if (timeZonePart != null) {
                            timeZone = StringUtils.parseTimeZoneString(timeZonePart);
                        }
                    }
                    catch (IllegalArgumentException ex) {
                        if (isRejectInvalidCookies() &&
                                request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) == null) {
                            throw new IllegalStateException("Encountered invalid locale cookie '" +
                                    cookieName + "': [" + value + "] due to: " + ex.getMessage());
                        }
                        else {
                            // Lenient handling (e.g. error dispatch): ignore locale/timezone parse exceptions
                            if (logger.isDebugEnabled()) {
                                logger.debug("Ignoring invalid locale cookie '" + cookieName +
                                        "': [" + value + "] due to: " + ex.getMessage());
                            }
                        }
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Parsed cookie value [" + cookie.getValue() + "] into locale '" + locale +
                                "'" + (timeZone != null ? " and time zone '" + timeZone.getID() + "'" : ""));
                    }
                }
            }

            request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME,
                    (locale != null ? locale : determineDefaultLocale(request)));
            request.setAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME,
                    (timeZone != null ? timeZone : determineDefaultTimeZone(request)));
        }
    }

    @Override
    public void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale) {
        setLocaleContext(request, response, (locale != null ? new SimpleLocaleContext(locale) : null));
    }

    @Override
    public void setLocaleContext(HttpServletRequest request, @Nullable HttpServletResponse response,
                                 @Nullable LocaleContext localeContext) {

        Assert.notNull(response, "HttpServletResponse is required for CookieLocaleResolver");

        Locale locale = null;
        TimeZone timeZone = null;
        if (localeContext != null) {
            locale = localeContext.getLocale();
            if (localeContext instanceof TimeZoneAwareLocaleContext) {
                timeZone = ((TimeZoneAwareLocaleContext) localeContext).getTimeZone();
            }
            addCookie(response,
                    (locale != null ? toLocaleValue(locale) : "-") + (timeZone != null ? '/' + timeZone.getID() : ""));
        }
        else {
            removeCookie(response);
        }
        request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME,
                (locale != null ? locale : determineDefaultLocale(request)));
        request.setAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME,
                (timeZone != null ? timeZone : determineDefaultTimeZone(request)));
    }


    /**
     * Parse the given locale value coming from an incoming cookie.
     * <p>The default implementation calls {@link StringUtils#parseLocale(String)},
     * accepting the {@link Locale#toString} format as well as BCP 47 language tags.
     * @param localeValue the locale value to parse
     * @return the corresponding {@code Locale} instance
     * @since 4.3
     * @see StringUtils#parseLocale(String)
     */
    @Nullable
    protected Locale parseLocaleValue(String localeValue) {
        return StringUtils.parseLocale(localeValue);
    }

    /**
     * Render the given locale as a text value for inclusion in a cookie.
     * <p>The default implementation calls {@link Locale#toString()}
     * or {@link Locale#toLanguageTag()}, depending on the
     * {@link #setLanguageTagCompliant "languageTagCompliant"} configuration property.
     * @param locale the locale to convert to a string
     * @return a String representation for the given locale
     * @since 4.3
     * @see #isLanguageTagCompliant()
     */
    protected String toLocaleValue(Locale locale) {
        return (isLanguageTagCompliant() ? locale.toLanguageTag() : locale.toString());
    }

    /**
     * Determine the default locale for the given request, called if no locale
     * cookie has been found.
     * <p>The default implementation returns the configured default locale, if any,
     * and otherwise falls back to the request's {@code Accept-Language} header
     * locale or the default locale for the server.
     * @param request the request to resolve the locale for
     * @return the default locale (never {@code null})
     * @see #setDefaultLocale
     * @see jakarta.servlet.http.HttpServletRequest#getLocale()
     */
    protected Locale determineDefaultLocale(HttpServletRequest request) {
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale == null) {
            defaultLocale = request.getLocale();
        }
        return defaultLocale;
    }

    /**
     * Determine the default time zone for the given request, called if no locale
     * cookie has been found.
     * <p>The default implementation returns the configured default time zone,
     * if any, or {@code null} otherwise.
     * @param request the request to resolve the time zone for
     * @return the default time zone (or {@code null} if none defined)
     * @see #setDefaultTimeZone
     */
    @Nullable
    protected TimeZone determineDefaultTimeZone(HttpServletRequest request) {
        return getDefaultTimeZone();
    }

}