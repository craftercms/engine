/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.service.context;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.HttpUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves the current site name from a cookie or
 * request param.
 *
 * @author avasquez
 */
public class CookieSiteResolver implements SiteResolver {

    private static final Log logger = LogFactory.getLog(CookieSiteResolver.class);

    protected String paramOrCookieName;

    @Required
    public void setParamOrCookieName(String paramOrCookieName) {
        this.paramOrCookieName = paramOrCookieName;
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        String siteName = request.getParameter(paramOrCookieName);
        if (StringUtils.isEmpty(siteName)) {
            siteName = HttpUtils.getCookieValue(paramOrCookieName, request);
            if (StringUtils.isEmpty(siteName) && logger.isDebugEnabled()) {
                logger.debug("No '" + paramOrCookieName + "' request param or cookie found");
            }
        }

        return siteName;
    }


}
