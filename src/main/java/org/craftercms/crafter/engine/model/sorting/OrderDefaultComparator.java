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
package org.craftercms.crafter.engine.model.sorting;

import org.craftercms.crafter.engine.model.SiteItem;

import java.util.Comparator;

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
        Float modelValue1 = null;
        Float modelValue2 = null;

        if (siteItem1.isFolder()) {
            siteItem1 = siteItem1.getChildItem("index.xml");
            if (siteItem1 != null) {
                modelValue1 = (Float) siteItem1.get(ORDER_DEFAULT_VALUE_KEY);
            }
        } else {
            modelValue1 = (Float) siteItem1.get(ORDER_DEFAULT_VALUE_KEY);
        }

        if (siteItem2.isFolder()) {
            siteItem2 = siteItem2.getChildItem("index.xml");
            if (siteItem2 != null) {
                modelValue2 = (Float) siteItem2.get(ORDER_DEFAULT_VALUE_KEY);
            }
        } else {
            modelValue2 = (Float) siteItem2.get(ORDER_DEFAULT_VALUE_KEY);
        }

        if (modelValue1 == null) {
            modelValue1 = DEFAULT_ORDER_DEFAULT_VALUE;
        }
        if (modelValue2 == null) {
            modelValue2 = DEFAULT_ORDER_DEFAULT_VALUE;
        }

        return modelValue1.compareTo(modelValue2);
    }

}
