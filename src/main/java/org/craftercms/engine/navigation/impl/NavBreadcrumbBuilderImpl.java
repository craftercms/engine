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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.converters.Converter;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.processors.impl.ItemProcessorPipeline;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.navigation.NavBreadcrumbBuilder;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.service.SiteItemService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link NavBreadcrumbBuilderImpl}.
 *
 * @author avasquez
 */
public class NavBreadcrumbBuilderImpl implements NavBreadcrumbBuilder {

    public static final String BREADCRUMB_CONST_KEY_ELEM = "breadcrumb";

    protected SiteItemService siteItemService;
    protected ItemProcessor processor;
    protected Converter<SiteItem, NavItem> defaultItemConverter;

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    public void setProcessor(ItemProcessor processor) {
        this.processor = processor;
    }

    public void setProcessors(List<ItemProcessor> processors) {
        processor = new ItemProcessorPipeline(processors);
    }

    public void setDefaultItemConverter(Converter<SiteItem, NavItem> defaultItemConverter) {
        this.defaultItemConverter = defaultItemConverter;
    }

    @Override
    public List<NavItem> getBreadcrumb(String url, String root) {
        return getBreadcrumb(url, root, null);
    }

    @Override
    public List<NavItem> getBreadcrumb(String url, String root, Converter<SiteItem, NavItem> itemConverter) {
        if (itemConverter == null) {
            itemConverter = defaultItemConverter;
        }

        List<NavItem> breadcrumb = new ArrayList<>();
        String breadcrumbUrl = extractBreadcrumbUrl(url, root);
        String[] breadcrumbUrlComponents = breadcrumbUrl.split("/");
        String currentUrl = root;

        for (String breadcrumbUrlComponent : breadcrumbUrlComponents) {
            currentUrl = UrlUtils.concat(currentUrl, breadcrumbUrlComponent);

            SiteItem siteItem = siteItemService.getSiteItem(currentUrl, processor);
            if (siteItem != null) {
                NavItem navItem = itemConverter.convert(siteItem);
                if (navItem != null) {
                    navItem.setActive(url.equals(siteItem.getItem().getDescriptorUrl()));
                    breadcrumb.add(navItem);
                }
            }
        }

        return breadcrumb;
    }

    protected String extractBreadcrumbUrl(String url, String root) {
        String indexFileName = SiteProperties.getIndexFileName();
        String breadcrumbUrl = StringUtils.substringBeforeLast(StringUtils.substringAfter(url, root), indexFileName);

        if (!breadcrumbUrl.startsWith("/")) {
            breadcrumbUrl = "/" + breadcrumbUrl;
        }

        return breadcrumbUrl;
    }

}
