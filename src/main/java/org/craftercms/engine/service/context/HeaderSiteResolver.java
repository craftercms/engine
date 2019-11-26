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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves the current site name from a custom HTTP
 * header.
 *
 * @author avasquez
 */
public class HeaderSiteResolver implements SiteResolver {

    private static final Logger logger = LoggerFactory.getLogger(HeaderSiteResolver.class);

    protected String headerName;

    @Required
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        String siteName = request.getHeader(headerName);
        if (StringUtils.isEmpty(siteName)) {
            logger.debug("No '{}' request header found", headerName);
        }

        return siteName;
    }

}
