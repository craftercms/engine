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
package org.craftercms.engine.navigation.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.commons.converters.Converter;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.processors.impl.ItemProcessorPipeline;
import org.craftercms.core.service.ItemFilter;
import org.craftercms.core.service.impl.CompositeItemFilter;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.navigation.NavTreeBuilder;
import org.craftercms.engine.service.SiteItemService;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link NavTreeBuilderImpl}.
 *
 * @author avasquez
 */
public class NavTreeBuilderImpl implements NavTreeBuilder {

    protected SiteItemService siteItemService;
    protected ItemFilter filter;
    protected ItemProcessor processor;
    protected Converter<SiteItem, NavItem> defaultItemConverter;

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    public void setFilter(ItemFilter filter) {
        this.filter = filter;
    }

    public void setProcessor(ItemProcessor processor) {
        this.processor = processor;
    }

    public void setFilters(List<ItemFilter> filters) {
        filter = new CompositeItemFilter(filters);
    }

    public void setProcessors(List<ItemProcessor> processors) {
        processor = new ItemProcessorPipeline(processors);
    }

    @Required
    public void setDefaultItemConverter(Converter<SiteItem, NavItem> defaultItemConverter) {
        this.defaultItemConverter = defaultItemConverter;
    }

    @Override
    public NavItem getNavTree(String url, int depth, String currentPageUrl) {
        return getNavTree(url, depth, currentPageUrl, null);
    }

    @Override
    public NavItem getNavTree(String url, int depth, String currentPageUrl,
                              Converter<SiteItem, NavItem> itemConverter) {
        if (itemConverter == null) {
            itemConverter = defaultItemConverter;
        }

        SiteItem treeRoot = siteItemService.getSiteTree(url, depth, filter, processor);

        return getNavItem(treeRoot, currentPageUrl, itemConverter);
    }

    protected NavItem getNavItem(SiteItem siteItem, String currentPageUrl, Converter<SiteItem, NavItem> itemConverter) {
        NavItem navItem = itemConverter.convert(siteItem);
        if (navItem != null) {
            navItem.setSubItems(getNavSubItems(siteItem, currentPageUrl, itemConverter));
            navItem.setActive(isActive(currentPageUrl, siteItem.getStoreUrl()));

            return navItem;
        } else {
            return null;
        }
    }

    protected List<NavItem> getNavSubItems(SiteItem siteItem, String currentPageUrl,
                                           Converter<SiteItem, NavItem> itemConverter) {
        List<SiteItem> childItems = siteItem.getChildItems();
        if (CollectionUtils.isNotEmpty(childItems)) {
            List<NavItem> navSubItems = new ArrayList<>();

            for (SiteItem childItem : childItems) {
                NavItem navSubItem = getNavItem(childItem, currentPageUrl, itemConverter);
                if (navSubItem != null && !navSubItems.contains(navSubItem)) {
                    navSubItems.add(navSubItem);
                }
            }

            return navSubItems;
        } else {
            return Collections.emptyList();
        }
    }

    protected boolean isActive(String currentPageUrl, String pageUrl) {
        if (!currentPageUrl.startsWith("/")) {
            currentPageUrl = "/" + currentPageUrl;
        }
        if (!currentPageUrl.endsWith("/")) {
            currentPageUrl += "/";
        }
        if (!pageUrl.startsWith("/")) {
            pageUrl = "/" + pageUrl;
        }
        if (!pageUrl.endsWith("/")) {
            pageUrl += "/";
        }

        return currentPageUrl.startsWith(pageUrl);
    }

}
