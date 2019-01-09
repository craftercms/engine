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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

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

    @Required
    public void setSiteNameRegex(String siteNameRegex) {
        this.siteNameRegex = siteNameRegex;
    }

    @Required
    public void setSiteNameRegexGroup(int siteNameRegexGroup) {
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
