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

package org.craftercms.engine.scripting.impl;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.SiteItemScriptResolver;
import org.craftercms.engine.service.context.SiteContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alfonso Vásquez
 */
public class SiteItemScriptResolverImplTest {

    private ContentStoreService storeService;
    private SiteItemScriptResolver scriptResolver;

    @Before
    public void setUp() throws Exception {
        storeService = createContentStoreService();
        scriptResolver = createScriptResolver(storeService);

        setCurrentRequest(createRequest());
        setCurrentSiteContext(createSiteContext());
    }

    @After
    public void tearDown() throws Exception {
        removeCurrentRequest();
        removeCurrentSiteContext();
    }

    @Test
    public void testGetScriptUrlsExisting() throws Exception {
        Item item = mock(Item.class);
        when(item.queryDescriptorValue("*/content-type")).thenReturn("/page/mypage1");
        when(item.queryDescriptorValues("*/scripts/item/key")).thenReturn(Arrays.asList(
            "/scripts/pages/script1.groovy"));

        SiteItem siteItem = mock(SiteItem.class);
        when(siteItem.getItem()).thenReturn(item);

        List<String> scriptUrls = scriptResolver.getScriptUrls(siteItem);
        assertNotNull(scriptUrls);
        assertEquals(2, scriptUrls.size());
        assertEquals("/scripts/pages/mypage1.groovy", scriptUrls.get(0));
        assertEquals("/scripts/pages/script1.groovy", scriptUrls.get(1));
    }

    @Test
    public void testGetScriptUrlNotFound() throws Exception {
        Item item = mock(Item.class);
        when(item.queryDescriptorValue("*/content-type")).thenReturn("/page/mypage2");
        when(item.queryDescriptorValues("*/scripts/item/key")).thenReturn(Arrays.asList(
            "/scripts/pages/script1.groovy"));

        SiteItem siteItem = mock(SiteItem.class);
        when(siteItem.getItem()).thenReturn(item);

        List<String> scriptUrls = scriptResolver.getScriptUrls(siteItem);
        assertNotNull(scriptUrls);
        assertEquals(1, scriptUrls.size());
        assertEquals("/scripts/pages/script1.groovy", scriptUrls.get(0));
    }

    @Test
    public void testGetScriptUrlError() throws Exception {
        Item item = mock(Item.class);
        when(item.queryDescriptorValue("*/content-type")).thenReturn("/page/mypage3");
        when(item.queryDescriptorValues("*/scripts/item/key")).thenReturn(Arrays.asList(
                "/scripts/pages/script1.groovy"));

        SiteItem siteItem = mock(SiteItem.class);
        when(siteItem.getItem()).thenReturn(item);

        List<String> scriptUrls = scriptResolver.getScriptUrls(siteItem);
        assertNotNull(scriptUrls);
        assertEquals(1, scriptUrls.size());
        assertEquals("/scripts/pages/script1.groovy", scriptUrls.get(0));
    }

    private ContentStoreService createContentStoreService() {
        ContentStoreService storeService = mock(ContentStoreService.class);
        when(storeService.exists(any(Context.class), eq("/scripts/pages/mypage1.groovy"))).thenReturn(true);
        when(storeService.exists(any(Context.class), eq("/scripts/pages/mypage2.groovy"))).thenReturn(false);
        when(storeService.exists(any(Context.class), eq("/scripts/pages/mypage3.groovy"))).thenThrow(new CrafterException());

        return storeService;
    }

    private SiteItemScriptResolver createScriptResolver(ContentStoreService storeService) {
        SiteItemScriptResolverImpl scriptResolver = new SiteItemScriptResolverImpl();
        scriptResolver.setContentTypePattern("^/page/(.+)$");
        scriptResolver.setContentTypeXPathQuery("*/content-type");
        scriptResolver.setScriptUrlFormat("/scripts/pages/%s.groovy");
        scriptResolver.setScriptsXPathQuery("*/scripts/item/key");
        scriptResolver.setStoreService(storeService);

        return scriptResolver;
    }

    private SiteContext createSiteContext()  {
        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getSiteName()).thenReturn("test");
        when(siteContext.getContext()).thenReturn(mock(Context.class));

        return siteContext;
    }

    private void setCurrentSiteContext(SiteContext siteContext)  {
        SiteContext.setCurrent(siteContext);
    }

    private void removeCurrentSiteContext() {
        SiteContext.clear();
    }

    private MockHttpServletRequest createRequest()  {
        MockHttpServletRequest request = new MockHttpServletRequest();

        return request;
    }

    private void setCurrentRequest(HttpServletRequest request) {
        RequestContext.setCurrent(new RequestContext(request, null, null));
    }

    private void removeCurrentRequest() {
        RequestContext.clear();
    }

}
