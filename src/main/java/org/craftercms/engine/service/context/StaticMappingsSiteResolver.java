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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves the current site name from a set of
 * static mappings, where the mapping key is a domain name, and the value is the site name.
 *
 * @author avasquez
 */
public class StaticMappingsSiteResolver implements SiteListResolver, SiteResolver {

    private static final Log logger = LogFactory.getLog(StaticMappingsSiteResolver.class);

    private Map<String, String> mappings;

    public StaticMappingsSiteResolver(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override
    public Collection<String> getSiteList() {
        return new LinkedHashSet<>(mappings.values());
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        String domainName = request.getServerName();

        if (mappings.containsKey(domainName)) {
            return mappings.get(domainName);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No site mapping found for domain name " + domainName);
            }

            return null;
        }
    }

}
