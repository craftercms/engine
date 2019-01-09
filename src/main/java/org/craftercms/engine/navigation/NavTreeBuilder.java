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

import org.craftercms.commons.converters.Converter;
import org.craftercms.engine.model.SiteItem;

/**
 * Creates navigation trees that facilitate the rendering of navigation.
 *
 * @author avasquez
 */
public interface NavTreeBuilder {

    /**
     * Returns the navigation tree with the specified depth for the specified store URL.
     *
     * @param url               the root folder of the tree
     * @param depth             the depth of the tree
     * @param currentPageUrl    the URL of the current page (used to determine the active URLs).
     *
     * @return the navigation tree
     */
    NavItem getNavTree(String url, int depth, String currentPageUrl);

    /**
     * Returns the navigation tree with the specified depth for the specified store URL.
     *
     * @param url               the root folder of the tree
     * @param depth             the depth of the tree
     * @param currentPageUrl    the URL of the current page (used to determine the active URLs).
     * @param itemConverter     the converter that should be used to convert from {@link SiteItem}s to the actual
     *                          {@link NavItem}s
     *
     * @return the navigation tree
     */
    NavItem getNavTree(String url, int depth, String currentPageUrl, Converter<SiteItem, NavItem> itemConverter);

}
