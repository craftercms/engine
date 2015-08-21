package org.craftercms.engine.targeting.impl;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.LocaleUtils;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.engine.targeting.CandidateTargetedUrlsResolver;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by alfonsovasquez on 21/8/15.
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

        exists = storeAdapter.exists(context, "/site/website/ja_JP_JP");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/ja_JP_JP/index.xml");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/site/website/index.xml");

        assertTrue(exists);

        exists = storeAdapter.exists(context, "/static-assets/css/main.css");

        assertTrue(exists);
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

        when(adapter.exists(any(Context.class), eq("/site/website/en/index.xml"))).thenReturn(true);

        when(adapter.exists(any(Context.class), eq("/static-assets/css/main.css"))).thenReturn(true);

        when(adapter.findContent(any(Context.class),
                                 any(CachingOptions.class),
                                 eq("/site/website/en/index.xml"))).thenReturn(mock(Content.class));

        when(adapter.findContent(any(Context.class), any(CachingOptions.class), eq("/static-assets/css/main.css")))
            .thenReturn(mock(Content.class));

        when(adapter.findItem(any(Context.class),
                              any(CachingOptions.class),
                              eq("/site/website/en"),
                              anyBoolean())).thenReturn(en);

        when(adapter.findItem(any(Context.class),
                              any(CachingOptions.class),
                              eq("/site/website/en/index.xml"),
                              anyBoolean())).thenReturn(enIdx);

        when(adapter.findItem(any(Context.class), any(CachingOptions.class), eq("/static-assets/css/main.css"),
                              anyBoolean())).thenReturn(mainCss);

        when(adapter.findItems(any(Context.class),
                               any(CachingOptions.class),
                               eq("/site/website/en"),
                               anyBoolean())).thenReturn(Arrays.asList(enIdx, enAboutUs));

        when(adapter.findItems(any(Context.class),
                               any(CachingOptions.class),
                               eq("/site/website/fr"),
                               anyBoolean())).thenReturn(Collections.singletonList(frIdx));

        return adapter;
    }

    private CandidateTargetedUrlsResolver createCandidateTargetedUrlsResolver() {
        LocaleTargetIdResolver targetIdResolver = new LocaleTargetIdResolver();

        TargetedUrlByFolderStrategy targetUrlStrategy = new TargetedUrlByFolderStrategy();
        targetUrlStrategy.setTargetIdResolver(targetIdResolver);

        CandidateTargetIdsResolverImpl candidateTargetIdsResolver = new CandidateTargetIdsResolverImpl();

        CandidateTargetedUrlsResolverImpl candidateUrlsResolver = new CandidateTargetedUrlsResolverImpl();
        candidateUrlsResolver.setTargetIdResolver(targetIdResolver);
        candidateUrlsResolver.setTargetedUrlStrategy(targetUrlStrategy);
        candidateUrlsResolver.setCandidateTargetIdsResolver(candidateTargetIdsResolver);

        return candidateUrlsResolver;
    }

}
