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

import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;

/**
 * @author joseross
 */
public class ExpectedConditionItemFilter implements ItemFilter {

    protected String conditionExpression;

    public ExpectedConditionItemFilter(final String conditionExpression) {
        this.conditionExpression = conditionExpression;
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
    public boolean accepts(final Item item, final List<Item> acceptedItems, final List<Item> rejectedItems,
                           final boolean runningBeforeProcessing) {
        if(item.getDescriptorDom() != null) {
            return item.getDescriptorDom()
                .createXPath(conditionExpression)
                .booleanValueOf(item.getDescriptorDom().getRootElement());
        } else {
            return true;
        }
    }
}
