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
package org.craftercms.engine.service.filter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;

/**
 * Accepts the item if the result of an node value XPath query matches an expected value regex.
 *
 * @author Alfonso Vásquez
 */
public class ExpectedNodeValueItemFilter implements ItemFilter {

    private String nodeXPathQuery;
    private String expectedValueRegex;

    public ExpectedNodeValueItemFilter(String nodeXPathQuery, String expectedValueRegex) {
        this.nodeXPathQuery = nodeXPathQuery;
        this.expectedValueRegex = expectedValueRegex;
    }

    @Override
    public boolean runBeforeProcessing() {
        return false;
    }

    @Override
    public boolean runAfterProcessing() {
        return true;
    }

    @Override
    public boolean accepts(Item item, List<Item> acceptedItems, List<Item> rejectedItems,
                           boolean runningBeforeProcessing) {
        // If the item doesn't have a descriptor, don't run the filter.
        if (item.getDescriptorDom() != null) {
            String result = item.queryDescriptorValue(nodeXPathQuery);
            if (StringUtils.isNotEmpty(result)) {
                return result.matches(expectedValueRegex);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return "ExpectedNodeValueItemFilter[" +
                "nodeXPathQuery='" + nodeXPathQuery + '\'' +
                ", expectedValueRegex='" + expectedValueRegex + '\'' +
                ']';
    }

}
