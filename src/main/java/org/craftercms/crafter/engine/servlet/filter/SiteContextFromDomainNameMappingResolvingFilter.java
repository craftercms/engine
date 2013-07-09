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

import java.util.Map;

/**
 * Filter that resolves the current site name from a mapping of the request domain name to site name.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextFromDomainNameMappingResolvingFilter extends AbstractSiteContextResolvingFilter {

    private static final Log logger = LogFactory.getLog(SiteContextFromDomainNameMappingResolvingFilter.class);

    private Map<String, String> domainNameToSiteNameMappings;

    @Required
    public void setDomainNameToSiteNameMappings(Map<String, String> domainNameToSiteNameMappings) {
        this.domainNameToSiteNameMappings = domainNameToSiteNameMappings;
    }

    @Override
    protected String getSiteNameFromRequest(ServletWebRequest request) {
        String domainName = request.getRequest().getServerName();

        if (domainNameToSiteNameMappings.containsKey(domainName)) {
            return domainNameToSiteNameMappings.get(domainName);
        } else {
            logger.warn("No site mapping found for domain name " + domainName);

            return null;
        }
    }

}
