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
package org.craftercms.engine.model.sorting;

import java.util.Comparator;

import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.properties.SiteProperties;

/**
 * Compares two {@link SiteItem}s through the "orderDefault_f" model value.
 *
 * @author Alfonso Vásquez
 */
public class OrderDefaultComparator implements Comparator<SiteItem> {

    public static final String ORDER_DEFAULT_VALUE_KEY = "orderDefault_f";
    public static final float DEFAULT_ORDER_DEFAULT_VALUE = -1;

    @Override
    public int compare(SiteItem siteItem1, SiteItem siteItem2) {
        Float orderDefault1 = getOrderDefault(siteItem1);
        Float orderDefault2 = getOrderDefault(siteItem2);

        if (orderDefault1 == null) {
            orderDefault1 = DEFAULT_ORDER_DEFAULT_VALUE;
        }
        if (orderDefault2 == null) {
            orderDefault2 = DEFAULT_ORDER_DEFAULT_VALUE;
        }

        return orderDefault1.compareTo(orderDefault2);
    }

    protected Float getOrderDefault(SiteItem siteItem) {
        Float value = (Float) siteItem.get(ORDER_DEFAULT_VALUE_KEY);

        if (value == null && siteItem.isFolder()) {
            siteItem = siteItem.getChildItem(SiteProperties.getIndexFileName());
            if (siteItem != null) {
                value = (Float) siteItem.get(ORDER_DEFAULT_VALUE_KEY);
            }
        }

        return value;
    }

}
