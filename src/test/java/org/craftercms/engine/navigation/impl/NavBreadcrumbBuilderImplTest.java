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

import org.craftercms.commons.converters.Converter;
import org.craftercms.core.service.Item;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.dom4j.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NavBreadcrumbBuilderImpl}.
 *
 * @author avasquez
 */
public class NavBreadcrumbBuilderImplTest {

    private NavBreadcrumbBuilderImpl navBreadcrumbBuilder;

    @Before
    public void setUp() throws Exception {
        navBreadcrumbBuilder = new NavBreadcrumbBuilderImpl();
        navBreadcrumbBuilder.setSiteItemService(getSiteItemService());
        navBreadcrumbBuilder.setDefaultItemConverter(getItemConverter());

        setUpCurrentSiteContext();
    }

    @Test
    public void testGetBreadcrumb() throws Exception {
        List<NavItem> breadcrumb = navBreadcrumbBuilder.getBreadcrumb("/site/website/en_US/about-us/leadership-team/index.xml",
                                                                      "/site/website/en_US");

        assertNotNull(breadcrumb);
        assertEquals(3, breadcrumb.size());
        assertEquals("/en_US", breadcrumb.get(0).getUrl());
        assertEquals("Home", breadcrumb.get(0).getLabel());
        assertEquals("/en/about-us", breadcrumb.get(1).getUrl());
        assertEquals("About Us", breadcrumb.get(1).getLabel());
        assertEquals("/en/about-us/leadership-team", breadcrumb.get(2).getUrl());
        assertEquals("Leadership Team", breadcrumb.get(2).getLabel());

        breadcrumb = navBreadcrumbBuilder.getBreadcrumb("/site/website/en_US/products/flagship", "/site/website/en_US");

        assertNotNull(breadcrumb);
        assertEquals(2, breadcrumb.size());
        assertEquals("/en_US", breadcrumb.get(0).getUrl());
        assertEquals("Home", breadcrumb.get(0).getLabel());
        assertEquals("/en_US/products/flagship", breadcrumb.get(1).getUrl());
        assertEquals("Flagship", breadcrumb.get(1).getLabel());
    }

    protected SiteItemService getSiteItemService() {
        SiteItem enUsItem = mock(SiteItem.class);
        when(enUsItem.getStoreUrl()).thenReturn("/site/website/en_US");
        when(enUsItem.get("navLabel")).thenReturn("Home");
        when(enUsItem.getItem()).thenReturn(mock(Item.class));
        when(enUsItem.getDom()).thenReturn(mock(Document.class));

        SiteItem aboutUsItem = mock(SiteItem.class);
        when(aboutUsItem.getStoreUrl()).thenReturn("/site/website/en/about-us");
        when(aboutUsItem.get("navLabel")).thenReturn("About Us");
        when(aboutUsItem.getItem()).thenReturn(mock(Item.class));
        when(aboutUsItem.getDom()).thenReturn(mock(Document.class));

        SiteItem leadershipTeamItem = mock(SiteItem.class);
        when(leadershipTeamItem.getStoreUrl()).thenReturn("/site/website/en/about-us/leadership-team");
        when(leadershipTeamItem.get("navLabel")).thenReturn("Leadership Team");
        when(leadershipTeamItem.getItem()).thenReturn(mock(Item.class));
        when(leadershipTeamItem.getDom()).thenReturn(mock(Document.class));

        SiteItem productsItem = mock(SiteItem.class);
        when(productsItem.getStoreUrl()).thenReturn("/site/website/en_US/products");

        SiteItem flagshipProductItem = mock(SiteItem.class);
        when(flagshipProductItem.getStoreUrl()).thenReturn("/site/website/en_US/products/flagship");
        when(flagshipProductItem.get("navLabel")).thenReturn("Flagship");
        when(flagshipProductItem.getItem()).thenReturn(mock(Item.class));
        when(flagshipProductItem.getDom()).thenReturn(mock(Document.class));

        SiteItemService siteItemService = mock(SiteItemService.class);
        when(siteItemService.getSiteItem("/site/website/en_US", null)).thenReturn(enUsItem);
        when(siteItemService.getSiteItem("/site/website/en_US/about-us", null)).thenReturn(aboutUsItem);
        when(siteItemService.getSiteItem("/site/website/en_US/about-us/leadership-team", null)).thenReturn(leadershipTeamItem);
        when(siteItemService.getSiteItem("/site/website/en_US/products", null)).thenReturn(productsItem);
        when(siteItemService.getSiteItem("/site/website/en_US/products/flagship", null)).thenReturn(flagshipProductItem);

        return siteItemService;
    }

    protected Converter<SiteItem, NavItem> getItemConverter() {
        Converter<SiteItem, NavItem> converter = mock(Converter.class);
        doAnswer((Answer<NavItem>) invocation -> {
            SiteItem siteItem = (SiteItem)invocation.getArguments()[0];
            NavItem navItem = null;

            if (siteItem.getDom() != null) {
                navItem = new NavItem();
                navItem.setUrl(siteItem.getStoreUrl().replace("/site/website", ""));
                navItem.setLabel((String)siteItem.get("navLabel"));
            }

            return navItem;
        }).when(converter).convert(any(SiteItem.class));

        return converter;
    }

    private void setUpCurrentSiteContext() {
        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getSiteName()).thenReturn("test");

        SiteContext.setCurrent(siteContext);
    }

}
