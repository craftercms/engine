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
package org.craftercms.engine.util.breadcrumb;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.Callback;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.core.service.Context;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;

/**
 * Helper class to create navigation breadcrumbs.
 *
 * @author Alfonso Vásquez
 *
 * @deprecated Please use instead {@link org.craftercms.engine.navigation.NavBreadcrumbBuilder}.
 */
@Deprecated
public class BreadcrumbBuilder {

    public static final String BREADCRUMB_CONST_KEY_ELEM = "breadcrumb";
    public static final String HOME_BREADCRUMB_NAME = "Home";

    protected CacheTemplate cacheTemplate;
    protected SiteItemService siteItemService;
    protected String homePath;
    protected String breadcrumbNameXPathQuery;

    public BreadcrumbBuilder(CacheTemplate cacheTemplate, final SiteItemService siteItemService, String homePath,
                             String breadcrumbNameXPathQuery) {
        this.cacheTemplate = cacheTemplate;
        this.siteItemService = siteItemService;
        this.homePath = homePath;
        this.breadcrumbNameXPathQuery = breadcrumbNameXPathQuery;
    }

    public List<BreadcrumbItem> buildBreadcrumb(final String url) {
        final Context context = SiteContext.getCurrent().getContext();

        return cacheTemplate.getObject(context, (Callback<List<BreadcrumbItem>>) () -> {
            String indexFileName = SiteProperties.getIndexFileName();
            CachingAwareList<BreadcrumbItem> breadcrumb = new CachingAwareList<>();
            String breadcrumbUrl = StringUtils.substringBeforeLast(StringUtils.substringAfter(url, homePath), indexFileName);
            String[] breadcrumbUrlComponents = breadcrumbUrl.split("/");
            String currentUrl = homePath;

            for (String breadcrumbUrlComponent : breadcrumbUrlComponents) {
                if (StringUtils.isNotEmpty(breadcrumbUrlComponent)) {
                    currentUrl += "/" + breadcrumbUrlComponent;
                }

                SiteItem siteItem = siteItemService.getSiteItem(UrlUtils.concat(currentUrl, indexFileName));

                if (siteItem != null && siteItem.getDom() != null) {
                    String breadcrumbName = siteItem.queryValue(breadcrumbNameXPathQuery);
                    if (StringUtils.isEmpty(breadcrumbName)) {
                        if (StringUtils.isNotEmpty(breadcrumbUrlComponent)) {
                            breadcrumbName = StringUtils.capitalize(breadcrumbUrlComponent.replace("-", " ").replace(".xml", ""));
                        } else {
                            breadcrumbName = HOME_BREADCRUMB_NAME;
                        }
                    }

                    breadcrumb.add(new BreadcrumbItem(currentUrl, breadcrumbName));
                }
            }

            return breadcrumb;
        }, url, BREADCRUMB_CONST_KEY_ELEM);
    }

}
