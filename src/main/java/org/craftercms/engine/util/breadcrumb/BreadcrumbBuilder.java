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
package org.craftercms.engine.util.breadcrumb;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.Callback;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;

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
    protected ContentStoreService storeService;
    protected String homePath;
    protected String breadcrumbNameXPathQuery;

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    @Required
    public void setBreadcrumbNameXPathQuery(String breadcrumbNameXPathQuery) {
        this.breadcrumbNameXPathQuery = breadcrumbNameXPathQuery;
    }

    public List<BreadcrumbItem> buildBreadcrumb(final String url) {
        final Context context = SiteContext.getCurrent().getContext();

        return cacheTemplate.getObject(context, new Callback<List<BreadcrumbItem>>() {

            @Override
            public List<BreadcrumbItem> execute() {
                String indexFileName = SiteProperties.getIndexFileName();
                CachingAwareList<BreadcrumbItem> breadcrumb = new CachingAwareList<BreadcrumbItem>();
                String breadcrumbUrl = StringUtils.substringBeforeLast(StringUtils.substringAfter(url, homePath), indexFileName);
                String[] breadcrumbUrlComponents = breadcrumbUrl.split("/");
                String currentUrl = homePath;

                for (String breadcrumbUrlComponent : breadcrumbUrlComponents) {
                    if (StringUtils.isNotEmpty(breadcrumbUrlComponent)) {
                        currentUrl += "/" + breadcrumbUrlComponent;
                    }

                    Item item = storeService.getItem(context, UrlUtils.concat(currentUrl, indexFileName));
                    if (item != null && item.getDescriptorDom() != null) {
                        String breadcrumbName = item.queryDescriptorValue(breadcrumbNameXPathQuery);
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
            }

        }, url, BREADCRUMB_CONST_KEY_ELEM);
    }

}
