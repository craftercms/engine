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

package org.craftercms.engine.util.predicates;

import java.util.List;

import org.apache.commons.collections4.Predicate;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;

/**
 * An implementation of Crafter's {@link org.craftercms.core.service.ItemFilter} that uses a predicate.
 *
 * @author avasquez
 */
public class PredicateBasedFilter implements ItemFilter {

    protected Predicate<Item> predicate;

    public PredicateBasedFilter(Predicate<Item> predicate) {
        this.predicate = predicate;
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
    public boolean accepts(Item item, List<Item> acceptedItems, List<Item> rejectedItems, boolean runningBeforeProcessing) {
        return predicate.evaluate(item);
    }

}
