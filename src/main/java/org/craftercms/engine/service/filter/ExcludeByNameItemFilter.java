/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.List;

import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;

/**
 * {@link ItemFilter} that rejects an item if its name matches any one of a list of regexes.
 *
 * @author Alfonso Vásquez
 */
public class ExcludeByNameItemFilter implements ItemFilter {

    private String[] excludeRegexes;

    @ConstructorProperties({"excludeRegex"})
    public ExcludeByNameItemFilter(String excludeRegex) {
        excludeRegexes = new String[1];
        excludeRegexes[0] = excludeRegex;
    }

    public ExcludeByNameItemFilter(String[] excludeRegexes) {
        this.excludeRegexes = excludeRegexes;
    }

    @Override
    public boolean runBeforeProcessing() {
        return true;
    }

    @Override
    public boolean runAfterProcessing() {
        return false;
    }

    @Override
    public boolean accepts(Item item, List<Item> acceptedItems, List<Item> rejectedItems,
                           boolean runningBeforeProcessing) {
        for (String regex : excludeRegexes) {
            if (item.getName().matches(regex)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "ExcludeByNameItemFilter[" +
                "excludeRegexes=" + (excludeRegexes == null ? null : Arrays.asList(excludeRegexes)) +
                ']';
    }

}
