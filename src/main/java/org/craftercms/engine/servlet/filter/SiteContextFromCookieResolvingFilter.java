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
package org.craftercms.engine.servlet.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.util.HttpServletUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.Cookie;

/**
 * Filter that resolves the current site name from a cookie or request param.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextFromCookieResolvingFilter extends AbstractSiteContextResolvingFilter {

    private static final Log logger = LogFactory.getLog(SiteContextFromCookieResolvingFilter.class);

    private String paramOrCookieName;

    @Required
    public void setParamOrCookieName(String paramOrCookieName) {
        this.paramOrCookieName = paramOrCookieName;
    }

    @Override
    public String getSiteNameFromRequest(ServletWebRequest request) {
        String siteName = request.getParameter(paramOrCookieName);
        if (StringUtils.isEmpty(siteName)) {
            Cookie cookie = HttpServletUtils.getCookie(paramOrCookieName, request.getRequest());
            if (cookie != null) {
                siteName = cookie.getValue();
            } else {
                logger.warn("No '" + paramOrCookieName + "' request param or cookie found");
            }
        }

        return siteName;
    }

}
