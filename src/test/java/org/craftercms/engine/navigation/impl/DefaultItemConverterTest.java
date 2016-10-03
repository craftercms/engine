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

import org.apache.commons.configuration.XMLConfiguration;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.UrlTransformationService;
import org.craftercms.engine.service.context.SiteContext;
import org.junit.Before;
import org.junit.Test;

import static org.craftercms.engine.util.config.TargetingProperties.TARGETING_ENABLED_CONFIG_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by alfonsovasquez on 30/9/16.
 */
public class DefaultItemConverterTest {

    private DefaultItemConverter itemConverter;

    @Before
    public void setUp() throws Exception {
        itemConverter = new DefaultItemConverter();
        itemConverter.setInternalNameXPath("*/internal-name");
        itemConverter.setNavLabelXPath("*/navLabel");
        itemConverter.setFolderToIndexFileTransformerName("folderToIndexFile");
        itemConverter.setToTargetedUrlTransformerName("toTargetedUrl");
        itemConverter.setStoreUrlToRenderUrlTransformerName("storeUrlToRenderUrl");
        itemConverter.setSiteItemService(createSiteItemService());
        itemConverter.setUrlTransformationService(createUrlTransformationService());

        setUpCurrentConfig();
    }

    @Test
    public void testConvert() throws Exception {
        SiteItem item = mock(SiteItem.class);
        when(item.getStoreUrl()).thenReturn("/site/website");
        when(item.isFolder()).thenReturn(true);

        NavItem navItem = itemConverter.convert(item);
        assertNotNull(navItem);
        assertEquals("Home", navItem.getLabel());
        assertEquals("/", navItem.getUrl());

        item = mock(SiteItem.class);
        when(item.getStoreUrl()).thenReturn("/site/website/about-us/index.xml");

        navItem = itemConverter.convert(item);
        assertNotNull(navItem);
        assertEquals("About Us", navItem.getLabel());
        assertEquals("/about-us", navItem.getUrl());

        item = mock(SiteItem.class);
        when(item.getStoreUrl()).thenReturn("/site/website/contact-us/index.xml");

        navItem = itemConverter.convert(item);
        assertNotNull(navItem);
        assertEquals("Contact us", navItem.getLabel());
        assertEquals("/contact-us", navItem.getUrl());
    }

    private UrlTransformationService createUrlTransformationService() {
        UrlTransformationService transformationService = mock(UrlTransformationService.class);
        when(transformationService.transform("folderToIndexFile", "/site/website"))
            .thenReturn("/site/website/index_en.xml");
        when(transformationService.transform("toTargetedUrl", "/site/website/about-us/index.xml"))
            .thenReturn("/site/website/about-us/index_en.xml");
        when(transformationService.transform("toTargetedUrl", "/site/website/contact-us/index.xml"))
            .thenReturn("/site/website/contact-us/index_en_US.xml");
        when(transformationService.transform("storeUrlToRenderUrl", "/site/website/index_en.xml"))
            .thenReturn("/");
        when(transformationService.transform("storeUrlToRenderUrl", "/site/website/about-us/index_en.xml"))
            .thenReturn("/about-us");
        when(transformationService.transform("storeUrlToRenderUrl", "/site/website/contact-us/index_en_US.xml"))
            .thenReturn("/contact-us");

        return transformationService;
    }

    private SiteItemService createSiteItemService() {
        SiteItem indexEn = mock(SiteItem.class);
        when(indexEn.getStoreUrl()).thenReturn("/site/website/index_en.xml");
        when(indexEn.queryValue("*/internal-name")).thenReturn("Home");

        SiteItem aboutUsIndexEn = mock(SiteItem.class);
        when(aboutUsIndexEn.getStoreUrl()).thenReturn("/site/website/about-us/index_en.xml");
        when(aboutUsIndexEn.queryValue("*/navLabel")).thenReturn("About Us");

        SiteItem contactUsIndexEnUs = mock(SiteItem.class);
        when(contactUsIndexEnUs.getStoreUrl()).thenReturn("/site/website/contact-us/index_en_US.xml");
        when(contactUsIndexEnUs.getStoreName()).thenReturn("contact-us.xml");

        SiteItemService siteItemService = mock(SiteItemService.class);
        when(siteItemService.getSiteItem("/site/website/index_en.xml")).thenReturn(indexEn);
        when(siteItemService.getSiteItem("/site/website/about-us/index_en.xml")).thenReturn(aboutUsIndexEn);
        when(siteItemService.getSiteItem("/site/website/contact-us/index_en_US.xml")).thenReturn(contactUsIndexEnUs);

        return siteItemService;
    }

    private void setUpCurrentConfig() {
        XMLConfiguration config = mock(XMLConfiguration.class);
        when(config.getBoolean(TARGETING_ENABLED_CONFIG_KEY, false)).thenReturn(true);

        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getSiteName()).thenReturn("test");
        when(siteContext.getConfig()).thenReturn(config);

        SiteContext.setCurrent(siteContext);
    }

}
