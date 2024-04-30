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
package org.craftercms.engine.service.context;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves the current site name from a custom HTTP
 * header.
 *
 * @author avasquez
 */
public class HeaderSiteResolver implements SiteResolver {

    private static final Logger logger = LoggerFactory.getLogger(HeaderSiteResolver.class);

    public HeaderSiteResolver(String headerName) {
        this.headerName = headerName;
    }

    protected String headerName;

    @Override
    public String getSiteName(HttpServletRequest request) {
        String siteName = request.getHeader(headerName);
        if (StringUtils.isEmpty(siteName)) {
            logger.debug("No '{}' request header found", headerName);
        }

        return siteName;
    }

}
