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

package org.craftercms.engine.targeting.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.LocaleUtils;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.engine.targeting.CandidateTargetedUrlsResolver;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.craftercms.engine.properties.SiteProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TargetedContentStoreAdapter}.
 *
 * @author avasquez
 */
public class TargetedContentStoreAdapterTest extends ConfigAwareTestBase {

    private TargetedContentStoreAdapter storeAdapter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        storeAdapter = new TargetedContentStoreAdapter();
        storeAdapter.setActualAdapter(createActualAdapter());
        storeAdapter.setCandidateTargetedUrlsResolver(createCandidateTargetedUrlsResolver());

        LocaleContextHolder.setLocale(LocaleUtils.toLocale("en"));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        LocaleContextHolder.resetLocaleContext();

        super.tearDown();
    }

    @Test
    public void testExists() throws Exception {
        Context context = new TargetedContentStoreAdapter.ContextWrapper(storeAdapter, mock(Context.class));

        boolean exists = storeAdapter.exists(context, "/site/website/en");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/ja_jp_jp");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/ja_jp_jp/index.xml");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/index.xml");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/static-assets/css/main.css");

        assertTrue(exists);
    }

    @Test
    public void testFindContent() throws Exception {
        Context context = new TargetedContentStoreAdapter.ContextWrapper(storeAdapter, mock(Context.class));
        CachingOptions cachingOptions = CachingOptions.DEFAULT_CACHING_OPTIONS;

        Content content = storeAdapter.findContent(context, cachingOptions, "/site/website/en/index.xml");

        assertNotNull(content);

        content = storeAdapter.findContent(context, cachingOptions, "/site/website/ja_jp_jp/index.xml");

        assertNotNull(content);

        content = storeAdapter.findContent(context, cachingOptions, "/site/website/index.xml");

        assertNotNull(content);

        content = storeAdapter.findContent(context, cachingOptions, "/static-assets/css/main.css");

        assertNotNull(content);
    }

    @Test
    public void testFindItem() throws Exception {
        Context context = new TargetedContentStoreAdapter.ContextWrapper(storeAdapter, mock(Context.class));
        CachingOptions cachingOptions = CachingOptions.DEFAULT_CACHING_OPTIONS;

        Item item = storeAdapter.findItem(context, cachingOptions, "/site/website/en", true);

        assertNotNull(item);
        assertEquals("en", item.getName());
        assertEquals("/site/website/en", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/site/website/ja_jp_jp", true);

        assertNotNull(item);
        assertEquals("en", item.getName());
        assertEquals("/site/website/en", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/site/website/ja_jp_jp/index.xml", true);

        assertNotNull(item);
        assertEquals("index.xml", item.getName());
        assertEquals("/site/website/en/index.xml", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/site/website/index.xml", true);

        assertNotNull(item);
        assertEquals("index.xml", item.getName());
        assertEquals("/site/website/index.xml", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/static-assets/css/main.css", true);

        assertNotNull(item);
        assertEquals("main.css", item.getName());
        assertEquals("/static-assets/css/main.css", item.getUrl());
    }

    @Test
    public void testFindItems() throws Exception {
        Context context = new TargetedContentStoreAdapter.ContextWrapper(storeAdapter, mock(Context.class));
        CachingOptions cachingOptions = CachingOptions.DEFAULT_CACHING_OPTIONS;

        List<Item> items = storeAdapter.findItems(context, cachingOptions, "/site/website/en", true);

        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("index.xml", items.get(0).getName());
        assertEquals("/site/website/en/index.xml", items.get(0).getUrl());
        assertEquals("about-us", items.get(1).getName());
        assertEquals("/site/website/en/about-us", items.get(1).getUrl());

        items = storeAdapter.findItems(context, cachingOptions, "/site/website/ja_jp_jp", true);

        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("index.xml", items.get(0).getName());
        assertEquals("/site/website/ja/index.xml", items.get(0).getUrl());
        assertEquals("about-us", items.get(1).getName());
        assertEquals("/site/website/en/about-us", items.get(1).getUrl());

        config.setProperty(SiteProperties.MERGE_FOLDERS_CONFIG_KEY, false);

        items = storeAdapter.findItems(context, cachingOptions, "/site/website/ja_jp_jp", true);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("index.xml", items.get(0).getName());
        assertEquals("/site/website/ja/index.xml", items.get(0).getUrl());
    }

    private ContentStoreAdapter createActualAdapter() {
        ContentStoreAdapter adapter = mock(ContentStoreAdapter.class);

        Item idx = new Item();
        idx.setName("index.xml");
        idx.setUrl("/site/website/index.xml");

        Item en = new Item();
        en.setName("en");
        en.setUrl("/site/website/en");

        Item enIdx = new Item();
        enIdx.setName("index.xml");
        enIdx.setUrl("/site/website/en/index.xml");

        Item enAboutUs = new Item();
        enAboutUs.setName("about-us");
        enAboutUs.setUrl("/site/website/en/about-us");

        Item mainCss = new Item();
        mainCss.setName("main.css");
        mainCss.setUrl("/static-assets/css/main.css");

        Item frIdx = new Item();
        frIdx.setName("index.xml");
        frIdx.setUrl("/site/website/ja/index.xml");

        when(adapter.exists(any(Context.class),
                            eq("/site/website/index.xml"))).thenReturn(true);

        when(adapter.exists(any(Context.class),
                            eq("/site/website/en"))).thenReturn(true);

        when(adapter.exists(any(Context.class),
                            eq("/site/website/en/index.xml"))).thenReturn(true);

        when(adapter.exists(any(Context.class),
                            eq("/static-assets/css/main.css"))).thenReturn(true);

        when(adapter.findContent(any(Context.class),
                                 any(CachingOptions.class),
                                 eq("/site/website/index.xml"))).thenReturn(mock(Content.class));

        when(adapter.findContent(any(Context.class),
                                 any(CachingOptions.class),
                                 eq("/site/website/en/index.xml"))).thenReturn(mock(Content.class));

        when(adapter.findContent(any(Context.class),
                                 any(CachingOptions.class),
                                 eq("/static-assets/css/main.css"))).thenReturn(mock(Content.class));

        when(adapter.findItem(any(Context.class), any(CachingOptions.class), eq("/site/website/index.xml"),
                              anyBoolean())).thenReturn(idx);

        when(adapter.findItem(any(Context.class),
                              any(CachingOptions.class),
                              eq("/site/website/en"),
                              anyBoolean())).thenReturn(en);

        when(adapter.findItem(any(Context.class),
                              any(CachingOptions.class),
                              eq("/site/website/en/index.xml"),
                              anyBoolean())).thenReturn(enIdx);

        when(adapter.findItem(any(Context.class),
                              any(CachingOptions.class),
                              eq("/static-assets/css/main.css"),
                              anyBoolean())).thenReturn(mainCss);

        when(adapter.findItems(any(Context.class),
                               any(CachingOptions.class),
                               eq("/site/website/en"),
                               anyBoolean())).thenReturn(Arrays.asList(enIdx, enAboutUs));

        when(adapter.findItems(any(Context.class),
                               any(CachingOptions.class),
                               eq("/site/website/ja"),
                               anyBoolean())).thenReturn(Collections.singletonList(frIdx));

        return adapter;
    }

    private CandidateTargetedUrlsResolver createCandidateTargetedUrlsResolver() {
        LocaleTargetIdManager targetIdManager = new LocaleTargetIdManager();

        TargetedUrlByFolderStrategy targetUrlStrategy = new TargetedUrlByFolderStrategy();
        targetUrlStrategy.setTargetIdManager(targetIdManager);

        CandidateTargetIdsResolverImpl candidateTargetIdsResolver = new CandidateTargetIdsResolverImpl();

        CandidateTargetedUrlsResolverImpl candidateUrlsResolver = new CandidateTargetedUrlsResolverImpl();
        candidateUrlsResolver.setTargetIdManager(targetIdManager);
        candidateUrlsResolver.setTargetedUrlStrategy(targetUrlStrategy);
        candidateUrlsResolver.setCandidateTargetIdsResolver(candidateTargetIdsResolver);

        return candidateUrlsResolver;
    }

}
