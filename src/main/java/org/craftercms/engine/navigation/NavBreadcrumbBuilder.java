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
package org.craftercms.engine.navigation;

import java.util.List;

import org.craftercms.commons.converters.Converter;
import org.craftercms.engine.model.SiteItem;

/**
 * Facilitates the creation of navigation breadcrumbs (e.g. Home > About Us > Leadership Team).
 *
 * @author avasquez
 */
public interface NavBreadcrumbBuilder {

    /**
     * Returns the navigation items that form the breadcrumb for the specified store URL.
     *
     * @param url   the current URL used to build the breadcrumb
     * @param root  the root URL, basically the starting point of the breadcrumb
     *
     * @return the list of {@link NavItem}s that represent the breadcrumb
     */
    List<NavItem> getBreadcrumb(String url, String root);

    /**
     * Returns the navigation items that form the breadcrumb for the specified store URL.
     *
     * @param url           the current URL used to build the breadcrumb
     * @param root          the root URL, basically the starting point of the breadcrumb
     * @param itemConverter the converter that should be used to convert from {@link SiteItem}s to the actual
     *                      {@link NavItem}s
     *
     * @return the list of {@link NavItem}s that represent the breadcrumb
     */
    List<NavItem> getBreadcrumb(String url, String root, Converter<SiteItem, NavItem> itemConverter);

}
