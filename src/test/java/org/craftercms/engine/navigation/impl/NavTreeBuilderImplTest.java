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

import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.converters.Converter;
import org.craftercms.core.service.ItemFilter;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.service.SiteItemService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NavTreeBuilderImpl}.
 *
 * @author avasquez
 */
public class NavTreeBuilderImplTest {

    private static final String ROOT_URL = "/site/website";
    private static final String URL1 = "/site/website/test";
    private static final String URL2 = "/site/website/test2";
    private static final String URL3 = "/site/website/test2/anothertest";

    private NavTreeBuilderImpl navTreeBuilder;

    @Before
    public void setUp() throws Exception {
        navTreeBuilder = new NavTreeBuilderImpl();
        navTreeBuilder.setSiteItemService(getSiteItemService());
        navTreeBuilder.setDefaultItemConverter(getItemConverter());
    }

    @Test
    public void testGetNavTree() {
        NavItem navItem = navTreeBuilder.getNavTree(ROOT_URL, -1, URL3);

        assertNotNull(navItem);
        assertEquals(ROOT_URL, navItem.getUrl());
        assertTrue(navItem.isActive());
        assertEquals(2, navItem.getSubItems().size());
        assertEquals(URL1, navItem.getSubItems().get(0).getUrl());
        assertFalse(navItem.getSubItems().get(0).isActive());
        assertEquals(URL2, navItem.getSubItems().get(1).getUrl());
        assertTrue(navItem.getSubItems().get(1).isActive());
        assertEquals(1, navItem.getSubItems().get(1).getSubItems().size());
        assertEquals(URL3, navItem.getSubItems().get(1).getSubItems().get(0).getUrl());
        assertTrue(navItem.getSubItems().get(1).getSubItems().get(0).isActive());
    }

    private SiteItemService getSiteItemService() {
        SiteItemService siteItemService = mock(SiteItemService.class);
        SiteItem item1 = createSiteItem(URL1);
        SiteItem item3 = createSiteItem(URL3);
        SiteItem item2 = createSiteItem(URL2, item3);
        SiteItem rootItem = createSiteItem(ROOT_URL, item1, item2);

        when(siteItemService.getSiteTree(ROOT_URL, -1, (ItemFilter) null, null)).thenReturn(rootItem);

        return siteItemService;
    }

    private Converter<SiteItem, NavItem> getItemConverter() {
        Converter<SiteItem, NavItem> converter = mock(Converter.class);
        doAnswer((Answer<NavItem>) invocation -> {
            SiteItem siteItem = (SiteItem)invocation.getArguments()[0];
            NavItem navItem = new NavItem();
            navItem.setLabel(siteItem.getStoreName());
            navItem.setUrl(siteItem.getStoreUrl());

            return navItem;
        }).when(converter).convert(any(SiteItem.class));

        return converter;
    }

    private SiteItem createSiteItem(String url, SiteItem... children) {
        SiteItem siteItem = mock(SiteItem.class);
        when(siteItem.getStoreName()).thenReturn(FilenameUtils.getName(url));
        when(siteItem.getStoreUrl()).thenReturn(url);

        if (children != null) {
            when(siteItem.getChildItems()).thenReturn(Arrays.asList(children));
        }

        return siteItem;
    }

}
