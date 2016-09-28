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
package org.craftercms.engine.service;

import java.util.Map;

import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ItemFilter;
import org.craftercms.engine.model.SiteItem;

/**
 * Service for accessing {@link org.craftercms.engine.model.SiteItem}s of the current site.
 *
 * @author Alfonso Vásquez
 */
public interface SiteItemService {

    /**
     * Returns the site item for the given URL
     *
     * @param url the URL of the item
     */
    SiteItem getSiteItem(String url);

    /**
     * Returns the site tree for the given URL. The item is expected to be a folder.
     *
     * @param url   the URL of the folder
     * @param depth the depth of the returned tree
     */
    SiteItem getSiteTree(String url, int depth);

    /**
     * Returns the site tree for the given URL. The item is expected to be a folder.
     *
     * @param url       the URL of the folder
     * @param depth     the depth of the returned tree
     * @param filter    a filter for the tree items
     * @param processor a processor for the tree items
     */
    SiteItem getSiteTree(String url, int depth, ItemFilter filter, ItemProcessor processor);

    /**
     * Returns the raw content of a site item.
     *
     * @param url   the URL of the item
     */
    Content getRawContent(String url);

    /**
     * Returns the site tree for the given URL. The item is expected to be a folder.
     *
     * @param url                   the URL of the folder
     * @param depth                 the depth of the returned tree
     * @param includeByNameRegex    a name regex for items to include
     * @param excludeByNameRegex    a name regex for items to exclude
     */
    @Deprecated
    SiteItem getSiteTree(String url, int depth, String includeByNameRegex, String excludeByNameRegex);

    /**
     * Returns the site tree for the given URL. The item is expected to be a folder.
     *
     * @param url                               the URL of the folder
     * @param depth                             the depth of the returned tree
     * @param includeByNameRegex                a name regex for items to include
     * @param excludeByNameRegex                a name regex for items to exclude
     * @param nodeXPathAndExpectedValuePairs    an X * 2 matrix where the first column is a node XPath and the
     *                                          second column is a expected value for that node. This XPath/value
     *                                          pairs are used to filter out items.
     *
     */
    @Deprecated
    SiteItem getSiteTree(String url, int depth, String includeByNameRegex, String excludeByNameRegex,
                         String[]... nodeXPathAndExpectedValuePairs);

    /**
     * Returns the site tree for the given URL. The item is expected to be a folder.
     *
     * @param url                               the URL of the folder
     * @param depth                             the depth of the returned tree
     * @param includeByNameRegex                a name regex for items to include
     * @param excludeByNameRegex                a name regex for items to exclude
     * @param nodeXPathAndExpectedValuePairs    a map where each key is a node XPath and each value is a expected value
     *                                          for that node. This XPath/value pairs are used to filter out items.
     */
    @Deprecated
    SiteItem getSiteTree(String url, int depth, String includeByNameRegex, String excludeByNameRegex,
                         Map<String, String> nodeXPathAndExpectedValuePairs);

}
