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

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Filter that resolves always the current site name to a default site name.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextFromDefaultResolvingFilter extends AbstractSiteContextResolvingFilter {

    private String defaultSiteName;

    @Required
    public void setDefaultSiteName(String defaultSiteName) {
        this.defaultSiteName = defaultSiteName;
    }

    @Override
    public String getSiteNameFromRequest(ServletWebRequest request) {
        return defaultSiteName;
    }

}
