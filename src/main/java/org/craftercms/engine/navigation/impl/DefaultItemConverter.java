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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.converters.Converter;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.UrlTransformationService;
import org.craftercms.engine.util.config.TargetingProperties;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 19/9/16.
 */
public class DefaultItemConverter implements Converter<SiteItem, NavItem> {

    protected String navLabelXPath;
    protected String internalNameXPath;
    protected String folderToIndexFileTransformerName;
    protected String toTargetedUrlTransformerName;
    protected String storeUrlToRenderUrlTransformerName;
    protected UrlTransformationService urlTransformationService;
    protected SiteItemService siteItemService;

    @Required
    public void setNavLabelXPath(String navLabelXPath) {
        this.navLabelXPath = navLabelXPath;
    }

    @Required
    public void setInternalNameXPath(String internalNameXPath) {
        this.internalNameXPath = internalNameXPath;
    }

    @Required
    public void setFolderToIndexFileTransformerName(String folderToIndexFileTransformerName) {
        this.folderToIndexFileTransformerName = folderToIndexFileTransformerName;
    }

    @Required
    public void setToTargetedUrlTransformerName(String toTargetedUrlTransformerName) {
        this.toTargetedUrlTransformerName = toTargetedUrlTransformerName;
    }

    @Required
    public void setStoreUrlToRenderUrlTransformerName(String storeUrlToRenderUrlTransformerName) {
        this.storeUrlToRenderUrlTransformerName = storeUrlToRenderUrlTransformerName;
    }

    @Required
    public void setUrlTransformationService(UrlTransformationService urlTransformationService) {
        this.urlTransformationService = urlTransformationService;
    }

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Override
    public NavItem convert(SiteItem siteItem) {
        NavItem navItem = new NavItem();

        if (siteItem.isFolder()) {
            siteItem = getIndexFile(siteItem);
        } else if (TargetingProperties.isTargetingEnabled()) {
            siteItem = getTargetedVersion(siteItem);
        }

        navItem.setLabel(getNavigationLabel(siteItem));
        navItem.setUrl(getNavigationUrl(siteItem));

        return navItem;
    }

    protected SiteItem getIndexFile(SiteItem folderItem) {
        String folderUrl = folderItem.getStoreUrl();
        String indexFileUrl = urlTransformationService.transform(folderToIndexFileTransformerName, folderUrl);

        return siteItemService.getSiteItem(indexFileUrl);
    }

    protected SiteItem getTargetedVersion(SiteItem siteItem) {
        String currentUrl = siteItem.getStoreUrl();
        String targetedUrl = urlTransformationService.transform(toTargetedUrlTransformerName, currentUrl);

        return siteItemService.getSiteItem(targetedUrl);
    }

    protected String getNavigationLabel(SiteItem siteItem) {
        String navLabel = siteItem.queryValue(navLabelXPath);
        if (StringUtils.isEmpty(navLabel)) {
            navLabel = siteItem.queryValue(internalNameXPath);
            if (StringUtils.isEmpty(navLabel)) {
                navLabel = StringUtils.stripEnd(siteItem.getStoreName(), ".xml");
                navLabel = StringUtils.replace(navLabel, "-", " ");
                navLabel = StringUtils.capitalize(navLabel);
            }
        }

        return navLabel;
    }

    protected String getNavigationUrl(SiteItem siteItem) {
        return urlTransformationService.transform(storeUrlToRenderUrlTransformerName, siteItem.getStoreUrl());
    }

}
