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
import java.util.Collections;
import jakarta.servlet.http.HttpServletRequest;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves always the current site name to a default
 * site name.
 *
 * @author avasquez
 */
public class DefaultSiteResolver implements SiteListResolver, SiteResolver {

    private String defaultSiteName;

    public DefaultSiteResolver(String defaultSiteName) {
        this.defaultSiteName = defaultSiteName;
    }

    @Override
    public Collection<String> getSiteList() {
        return Collections.singleton(defaultSiteName);
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        return defaultSiteName;
    }

}
