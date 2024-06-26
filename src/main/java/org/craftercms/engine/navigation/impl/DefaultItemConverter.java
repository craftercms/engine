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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.converters.Converter;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.service.UrlTransformationService;

/**
 * Default converter from {@link SiteItem} to {@link NavItem}. To generate the URL, it uses a URL transformer, and to generate the
 * navigation label it uses the nav label element in the content, the internal name element or the file name, in that order. If the
 * {@link SiteItem} has no content, null is returned (items with no content, like folders with no index.xml, should not be navigable).
 *
 * @author avasquez
 */
public class DefaultItemConverter implements Converter<SiteItem, NavItem> {
    
    protected String navLabelXPath;
    protected String internalNameXPath;
    protected String storeUrlToRenderUrlTransformerName;
    protected UrlTransformationService urlTransformationService;

    public DefaultItemConverter(String navLabelXPath, String internalNameXPath, String storeUrlToRenderUrlTransformerName,
                                UrlTransformationService urlTransformationService) {
        this.navLabelXPath = navLabelXPath;
        this.internalNameXPath = internalNameXPath;
        this.storeUrlToRenderUrlTransformerName = storeUrlToRenderUrlTransformerName;
        this.urlTransformationService = urlTransformationService;
    }

    @Override
    public Class<?> getSourceClass() {
        return SiteItem.class;
    }

    @Override
    public Class<?> getTargetClass() {
        return NavItem.class;
    }

    @Override
    public NavItem convert(SiteItem siteItem) {
        NavItem navItem = null;

        if (siteItem.getDom() != null) {
            navItem = new NavItem();
            navItem.setLabel(getNavigationLabel(siteItem));
            navItem.setUrl(getNavigationUrl(siteItem));
            navItem.setAttributes(getAdditionalAttributes(siteItem));
        }

        return navItem;
    }

    protected String getNavigationLabel(SiteItem siteItem) {
        String navLabel = siteItem.queryValue(navLabelXPath);
        if (StringUtils.isEmpty(navLabel)) {
            navLabel = siteItem.queryValue(internalNameXPath);
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

    protected Map<String, String> getAdditionalAttributes(SiteItem siteItem) {
        Map<String, String> attrs = new HashMap<>();
        String[] fields = SiteProperties.getNavigationAdditionalFields();
        for (String field : fields) {
            String value = siteItem.queryValue(field);
            if(value != null) {
                attrs.put(field, value);
            }
        }
        return attrs;
    }

    @Override
    public String toString() {
        return "DefaultItemConverter{" +
               "navLabelXPath='" + navLabelXPath + '\'' +
               ", internalNameXPath='" + internalNameXPath + '\'' +
               ", storeUrlToRenderUrlTransformerName='" + storeUrlToRenderUrlTransformerName + '\'' +
               ", urlTransformationService=" + urlTransformationService +
               '}';
    }

}
