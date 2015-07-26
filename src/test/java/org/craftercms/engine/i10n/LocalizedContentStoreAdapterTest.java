package org.craftercms.engine.i10n;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.LocaleUtils;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
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
 * Unit tests for {@link LocalizedContentStoreAdapter}.
 *
 * @author avasquez
 */
public class LocalizedContentStoreAdapterTest extends ConfigAwareTestBase {

    private LocalizedContentStoreAdapter storeAdapter;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        storeAdapter = new LocalizedContentStoreAdapter();
        storeAdapter.setActualAdapter(createActualAdapter());

        LocaleContextHolder.setLocale(LocaleUtils.toLocale("en"));
    }

    @Test
    public void testExists() throws Exception {
        Context context = mock(Context.class);

        boolean exists = storeAdapter.exists(context, "/site/website/en");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/th_TH_TH");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/th_TH_TH/index.xml");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/index.xml");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/about-us");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/static-assets/css/main.css");

        assertTrue(exists);
    }

    @Test
    public void testFindContent() throws Exception {
        Context context = mock(Context.class);
        CachingOptions cachingOptions = CachingOptions.DEFAULT_CACHING_OPTIONS;

        Content content = storeAdapter.findContent(context, cachingOptions, "/site/website/en/index.xml");

        assertNotNull(content);

        content = storeAdapter.findContent(context, cachingOptions, "/site/website/th_TH_TH/index.xml");

        assertNotNull(content);

        content = storeAdapter.findContent(context, cachingOptions, "/site/website/index.xml");

        assertNotNull(content);

        content = storeAdapter.findContent(context, cachingOptions, "/site/website/about-us");

        assertNotNull(content);

        content = storeAdapter.findContent(context, cachingOptions, "/static-assets/css/main.css");

        assertNotNull(content);
    }

    @Test
    public void testFindItem() throws Exception {
        Context context = mock(Context.class);
        CachingOptions cachingOptions = CachingOptions.DEFAULT_CACHING_OPTIONS;

        Item item = storeAdapter.findItem(context, cachingOptions, "/site/website/en", true);

        assertNotNull(item);
        assertEquals("en", item.getName());
        assertEquals("/site/website/en", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/site/website/th_TH_TH", true);

        assertNotNull(item);
        assertEquals("en", item.getName());
        assertEquals("/site/website/en", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/site/website/th_TH_TH/index.xml", true);

        assertNotNull(item);
        assertEquals("index.xml", item.getName());
        assertEquals("/site/website/en/index.xml", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/site/website/index.xml", true);

        assertNotNull(item);
        assertEquals("index.xml", item.getName());
        assertEquals("/site/website/en/index.xml", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/site/website/about-us", true);

        assertNotNull(item);
        assertEquals("about-us", item.getName());
        assertEquals("/site/website/en/about-us", item.getUrl());

        item = storeAdapter.findItem(context, cachingOptions, "/static-assets/css/main.css", true);

        assertNotNull(item);
        assertEquals("main.css", item.getName());
        assertEquals("/static-assets/css/main.css", item.getUrl());
    }

    @Test
    public void testFindItems() throws Exception {
        Context context = mock(Context.class);
        CachingOptions cachingOptions = CachingOptions.DEFAULT_CACHING_OPTIONS;

        List<Item> items = storeAdapter.findItems(context, cachingOptions, "/site/website/en", true);

        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("index.xml", items.get(0).getName());
        assertEquals("/site/website/en/index.xml", items.get(0).getUrl());
        assertEquals("about-us", items.get(1).getName());
        assertEquals("/site/website/en/about-us", items.get(1).getUrl());

        items = storeAdapter.findItems(context, cachingOptions, "/site/website/fr_CA", true);

        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("index.xml", items.get(0).getName());
        assertEquals("/site/website/fr/index.xml", items.get(0).getUrl());
        assertEquals("about-us", items.get(1).getName());
        assertEquals("/site/website/en/about-us", items.get(1).getUrl());

        config.setProperty(LocalizedContentStoreAdapter.I10N_MERGE_FOLDERS_CONFIG_KEY, false);

        items = storeAdapter.findItems(context, cachingOptions, "/site/website/fr_CA", true);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("index.xml", items.get(0).getName());
        assertEquals("/site/website/fr/index.xml", items.get(0).getUrl());
    }

    private ContentStoreAdapter createActualAdapter() {
        ContentStoreAdapter adapter = mock(ContentStoreAdapter.class);

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
        frIdx.setUrl("/site/website/fr/index.xml");

        when(adapter.exists(any(Context.class),
                            eq("/site/website/en"))).thenReturn(true);

        when(adapter.exists(any(Context.class),
                            eq("/site/website/en/index.xml"))).thenReturn(true);

        when(adapter.exists(any(Context.class),
                            eq("/site/website/en/about-us"))).thenReturn(true);

        when(adapter.exists(any(Context.class),
                            eq("/static-assets/css/main.css"))).thenReturn(true);

        when(adapter.findContent(any(Context.class),
                                 any(CachingOptions.class),
                                 eq("/site/website/en/index.xml"))).thenReturn(mock(Content.class));

        when(adapter.findContent(any(Context.class),
                                 any(CachingOptions.class),
                                 eq("/site/website/en/about-us"))).thenReturn(mock(Content.class));

        when(adapter.findContent(any(Context.class),
                                 any(CachingOptions.class),
                                 eq("/static-assets/css/main.css"))).thenReturn(mock(Content.class));

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
                              eq("/site/website/en/about-us"),
                              anyBoolean())).thenReturn(enAboutUs);

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
                               eq("/site/website/fr"),
                               anyBoolean())).thenReturn(Arrays.asList(frIdx));

        return adapter;
    }

}
