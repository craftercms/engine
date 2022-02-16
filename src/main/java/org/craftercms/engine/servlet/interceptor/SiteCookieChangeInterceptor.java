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

package org.craftercms.engine.servlet.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.beans.ConstructorProperties;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Handler interceptor that will set the site cookie whenever the cookie is missing or the current site is different
 * that the cookie value. Use in conjunction with {@link org.craftercms.engine.service.context.CookieSiteResolver}
 * when you want to set up simple multi tenancy.
 *
 * @author avasquez
 */
public class SiteCookieChangeInterceptor extends HandlerInterceptorAdapter {

    private static final Log logger = LogFactory.getLog(SiteCookieChangeInterceptor.class);

    protected boolean enabled;

    protected String cookieName;

    protected String cookieDomain;

    protected String cookiePath;

    protected int cookieMaxAge;

    protected boolean httpOnly;

    protected boolean secure;

    @ConstructorProperties({"enabled", "cookieName", "cookieDomain", "cookiePath", "cookieMaxAge", "httpOnly",
            "secure"})
    public SiteCookieChangeInterceptor(final boolean enabled, final String cookieName, final String cookieDomain,
                                       final String cookiePath, final int cookieMaxAge, final boolean httpOnly,
                                       final boolean secure) {
        this.enabled = enabled;
        this.cookieName = cookieName;
        this.cookieDomain = cookieDomain;
        this.cookiePath = cookiePath;
        this.cookieMaxAge = cookieMaxAge;
        this.httpOnly = httpOnly;
        this.secure = secure;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        if (!enabled) {
            return;
        }
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            String siteName = siteContext.getSiteName();
            String cookieValue = HttpUtils.getCookieValue(cookieName, request);

            if (!siteName.equals(cookieValue)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting '" + cookieName + "' cookie to '" + siteName + "'");
                }

                Cookie cookie = new Cookie(cookieName, siteName);
                if (isEmpty(cookieDomain)) {
                    cookie.setDomain(request.getServerName());
                } else {
                    cookie.setDomain(cookieDomain);
                }
                cookie.setPath(cookiePath);
                cookie.setMaxAge(cookieMaxAge);
                cookie.setHttpOnly(httpOnly);
                cookie.setSecure(secure);

                response.addCookie(cookie);
            }
        } else {
            throw new IllegalStateException("No current site context found");
        }
    }
}
