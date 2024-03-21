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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves the current site name from an extract of
 * the request URI.
 *
 * @author avasquez
 */
public class RequestUriSiteResolver implements SiteResolver {

    private static final Log logger = LogFactory.getLog(RequestUriSiteResolver.class);

    protected String siteNameRegex;
    protected int siteNameRegexGroup;

    public RequestUriSiteResolver(String siteNameRegex, int siteNameRegexGroup) {
        this.siteNameRegex = siteNameRegex;
        this.siteNameRegexGroup = siteNameRegexGroup;
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        Matcher matcher = Pattern.compile(siteNameRegex).matcher(requestUri);
        String siteName = null;

        if (matcher.matches()) {
            siteName = matcher.group(siteNameRegexGroup);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Unable to match request URI " + requestUri + " to regex " + siteNameRegex);
        }

        return siteName;
    }

}
