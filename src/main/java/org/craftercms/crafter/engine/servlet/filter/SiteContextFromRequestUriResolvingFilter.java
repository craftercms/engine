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
package org.craftercms.crafter.engine.servlet.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter that resolves the current site name from an extract of the request URI.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextFromRequestUriResolvingFilter extends AbstractSiteContextResolvingFilter {

    private static final Log logger = LogFactory.getLog(SiteContextFromRequestUriResolvingFilter.class);

    private String siteNameRegex;
    private int siteNameRegexGroup;

    @Required
    public void setSiteNameRegex(String siteNameRegex) {
        this.siteNameRegex = siteNameRegex;
    }

    @Required
    public void setSiteNameRegexGroup(int siteNameRegexGroup) {
        this.siteNameRegexGroup = siteNameRegexGroup;
    }

    @Override
    public String getSiteNameFromRequest(ServletWebRequest request) {
        String requestUri = request.getRequest().getRequestURI();
        Matcher matcher = Pattern.compile(siteNameRegex).matcher(requestUri);
        String siteName = null;

        if (matcher.matches()) {
            siteName = matcher.group(siteNameRegexGroup);
        } else {
            logger.warn("Unable to match request URI " + requestUri + " to regex " + siteNameRegex);
        }

        return siteName;
    }

}
