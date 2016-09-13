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
import org.craftercms.core.service.ItemFilter;
import org.craftercms.engine.model.SiteItem;

/**
 * Service for accessing {@link org.craftercms.engine.model.SiteItem}s of the current site.
 *
 * @author Alfonso Vásquez
 */
public interface SiteItemService {

    SiteItem getSiteItem(String url);

    SiteItem getSiteTree(String url, int depth);

    SiteItem getSiteTree(String url, int depth, String includeByNameRegex, String excludeByNameRegex);

    @Deprecated
    SiteItem getSiteTree(String url, int depth, String includeByNameRegex, String excludeByNameRegex,
                         String[]... nodeXPathAndExpectedValuePairs);

    SiteItem getSiteTree(String url, int depth, String includeByNameRegex, String excludeByNameRegex,
                         Map<String, String> nodeXPathAndExpectedValuePairs);

    SiteItem getSiteTree(String url, int depth, ItemFilter filter, ItemProcessor processor);

}
