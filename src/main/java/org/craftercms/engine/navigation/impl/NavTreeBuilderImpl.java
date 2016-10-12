/*
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 19/9/16.
 */
public class NavTreeBuilderImpl implements NavTreeBuilder {

    private static final Logger logger = LoggerFactory.getLogger(NavTreeBuilderImpl.class);

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
        navItem.setSubItems(getNavSubItems(siteItem, currentPageUrl, itemConverter));
        navItem.setActive(currentPageUrl.startsWith(siteItem.getStoreUrl()));

        return navItem;
    }

    protected List<NavItem> getNavSubItems(SiteItem siteItem, String currentPageUrl,
                                           Converter<SiteItem, NavItem> itemConverter) {
        List<SiteItem> childItems = siteItem.getChildItems();
        if (CollectionUtils.isNotEmpty(childItems)) {
            List<NavItem> navSubItems = new ArrayList<>();

            for (SiteItem childItem : childItems) {
                NavItem navSubItem = getNavItem(childItem, currentPageUrl, itemConverter);
                if (!navSubItems.contains(navSubItem)) {
                    navSubItems.add(navSubItem);
                }
            }

            return navSubItems;
        } else {
            return Collections.emptyList();
        }
    }

}
