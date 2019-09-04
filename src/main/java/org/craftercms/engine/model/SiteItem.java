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

package org.craftercms.engine.model;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.craftercms.core.service.Item;
import org.dom4j.Document;

/**
 * Defines the basic behavior for a site item
 *
 * @author joseross
 * @since 3.1.2
 */
public interface SiteItem {

    Item getItem();

    String getStoreName();

    String getStoreUrl();

    boolean isFolder();

    Document getDom();

    Map<String, Object> getProperties();

    Object get(String xpathExpression);

    String queryValue(String xpathExpression);

    List<String> queryValues(String xpathExpression);

    String queryValue(String xpathExpression, Map<String, String> namespaceUris);

    List<String> queryValues(String xpathExpression, Map<String, String> namespaceUris);

    List<SiteItem> getChildItems();

    SiteItem getChildItem(String storeName);

    List<SiteItem> sortItems(List<SiteItem> items, Comparator<SiteItem> comparator);

    SiteItem createItemWrapper(Item item);

}
