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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.converters.Converter;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.service.UrlTransformationService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 19/9/16.
 */
public class DefaultItemConverter implements Converter<SiteItem, NavItem> {

    protected String navLabelXPath;
    protected String internalNameXPath;
    protected String storeUrlToRenderUrlTransformerName;
    protected UrlTransformationService urlTransformationService;

    @Required
    public void setNavLabelXPath(String navLabelXPath) {
        this.navLabelXPath = navLabelXPath;
    }

    @Required
    public void setInternalNameXPath(String internalNameXPath) {
        this.internalNameXPath = internalNameXPath;
    }

    @Required
    public void setStoreUrlToRenderUrlTransformerName(String storeUrlToRenderUrlTransformerName) {
        this.storeUrlToRenderUrlTransformerName = storeUrlToRenderUrlTransformerName;
    }

    @Required
    public void setUrlTransformationService(UrlTransformationService urlTransformationService) {
        this.urlTransformationService = urlTransformationService;
    }

    @Override
    public NavItem convert(SiteItem siteItem) {
        NavItem navItem = new NavItem();
        navItem.setLabel(getNavigationLabel(siteItem));
        navItem.setUrl(getNavigationUrl(siteItem));

        return navItem;
    }

    protected String getNavigationLabel(SiteItem siteItem) {
        String navLabel = siteItem.getItem().queryDescriptorValue(navLabelXPath);
        if (StringUtils.isEmpty(navLabel)) {
            navLabel = siteItem.getItem().queryDescriptorValue(internalNameXPath);
            if (StringUtils.isEmpty(navLabel)) {
                navLabel = FilenameUtils.removeExtension(siteItem.getStoreName());
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
