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
package org.craftercms.engine.navigation.impl;

import org.apache.commons.lang.StringUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.LocaleUtils;

import java.beans.ConstructorProperties;
import java.util.List;

import static org.craftercms.commons.locale.LocaleUtils.parseLocale;
import static org.craftercms.engine.util.LocaleUtils.getCompatibleLocales;

/**
 * Implementation of {@link ItemFilter} that checks if a given {@link Item} is compatible with the configured locales
 *
 * @author joseross
 * @since 4.0.0
 */
public class LocaleItemFilter implements ItemFilter {

    protected String localeCodeXPathQuery;

    protected ContentStoreService contentStoreService;

    @ConstructorProperties({"localeCodeSelector", "contentStoreService"})
    public LocaleItemFilter(String localeCodeSelector, ContentStoreService contentStoreService) {
        this.localeCodeXPathQuery = localeCodeSelector;
        this.contentStoreService = contentStoreService;
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
        // If there is no descriptor, accept it
        if (item.getDescriptorDom() == null) {
            return true;
        }

        // If it has a compatible version, accept it
        var itemUrl = item.getDescriptorUrl();
        var localeUrl = LocaleUtils.resolveLocalePath(itemUrl,
                url -> contentStoreService.exists(SiteContext.getCurrent().getContext(), url));

        if (!StringUtils.equals(itemUrl, localeUrl)) {
            return true;
        }

        // If it doesn't have a compatible version, check if the locale code is compatible
        var itemLocale = parseLocale(item.queryDescriptorValue(localeCodeXPathQuery));
        if (itemLocale != null) {
            return getCompatibleLocales().contains(itemLocale);
        }

        // If it doesn't have a locale code then accept it for backward compatibility
        return true;
    }

}
