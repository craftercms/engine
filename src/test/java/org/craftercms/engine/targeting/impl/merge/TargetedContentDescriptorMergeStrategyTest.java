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

package org.craftercms.engine.targeting.impl.merge;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategy;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategyResolver;
import org.craftercms.core.xml.mergers.MergeableDescriptor;
import org.craftercms.core.xml.mergers.impl.resolvers.UrlPatternMergeStrategyResolver;
import org.craftercms.core.xml.mergers.impl.strategies.InheritLevelsMergeStrategy;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.targeting.CandidateTargetedUrlsResolver;
import org.craftercms.engine.targeting.impl.CandidateTargetIdsResolverImpl;
import org.craftercms.engine.targeting.impl.CandidateTargetedUrlsResolverImpl;
import org.craftercms.engine.targeting.impl.LocaleTargetIdManager;
import org.craftercms.engine.targeting.impl.TargetedUrlByFolderStrategy;
import org.dom4j.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.craftercms.engine.properties.SiteProperties.AVAILABLE_TARGET_IDS_CONFIG_KEY;
import static org.craftercms.engine.properties.SiteProperties.FALLBACK_ID_CONFIG_KEY;
import static org.craftercms.engine.properties.SiteProperties.ROOT_FOLDERS_CONFIG_KEY;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TargetedContentDescriptorMergeStrategy}.
 *
 * @author avasquez
 */
public class TargetedContentDescriptorMergeStrategyTest {

    private static final String SITE_NAME = "test";
    private static final String LEVEL_DESCRIPTOR_FILENAME = "crafter-level-descriptor.level.xml";
    private static final String[] ROOT_FOLDERS = { "/site/website" };
    private static final String[] AVAILABLE_TARGET_IDS = { "es_cr", "es", "en" };
    private static final String FALLBACK_TARGET_ID = "en";

    private TargetedContentDescriptorMergeStrategy mergeStrategy;

    @Before
    public void setUp() throws Exception {
        mergeStrategy = new TargetedContentDescriptorMergeStrategy();
        mergeStrategy.setLevelDescriptorFileName(LEVEL_DESCRIPTOR_FILENAME);
        mergeStrategy.setMergeStrategyResolver(createStrategyResolver(mergeStrategy));
        mergeStrategy.setCandidateTargetedUrlsResolver(createCandidateUrlsResolver());

        setUpCurrentSiteContext();
    }

    @After
    public void tearDown() throws Exception {
        clearCurrentSiteContext();
    }

    @Test
    public void testGetDescriptors() throws Exception {
        Item item = mock(Item.class);
        when(item.getDescriptorDom()).thenReturn(mock(Document.class));

        ContentStoreAdapter storeAdapter = mock(ContentStoreAdapter.class);
        when(storeAdapter.findItem(any(Context.class), any(CachingOptions.class), anyString(),
                                   anyBoolean())).thenReturn(item);

        Context context = mock(Context.class);
        when(context.getStoreAdapter()).thenReturn(storeAdapter);

        List<MergeableDescriptor> descriptors = mergeStrategy.getDescriptors(context,
                                                                             CachingOptions.DEFAULT_CACHING_OPTIONS,
                                                                             "/site/website/es_cr/products/index.xml",
                                                                             mock(Document.class), false);

        assertNotNull(descriptors);
        assertEquals(12, descriptors.size());
        assertEquals("/crafter-level-descriptor.level.xml", descriptors.get(0).getUrl());
        assertEquals("/site/crafter-level-descriptor.level.xml", descriptors.get(1).getUrl());
        assertEquals("/site/website/crafter-level-descriptor.level.xml", descriptors.get(2).getUrl());
        assertEquals("/site/website/en/crafter-level-descriptor.level.xml", descriptors.get(3).getUrl());
        assertEquals("/site/website/en/products/crafter-level-descriptor.level.xml", descriptors.get(4).getUrl());
        assertEquals("/site/website/en/products/index.xml", descriptors.get(5).getUrl());
        assertEquals("/site/website/es/crafter-level-descriptor.level.xml", descriptors.get(6).getUrl());
        assertEquals("/site/website/es/products/crafter-level-descriptor.level.xml", descriptors.get(7).getUrl());
        assertEquals("/site/website/es/products/index.xml", descriptors.get(8).getUrl());
        assertEquals("/site/website/es_cr/crafter-level-descriptor.level.xml", descriptors.get(9).getUrl());
        assertEquals("/site/website/es_cr/products/crafter-level-descriptor.level.xml", descriptors.get(10).getUrl());
        assertEquals("/site/website/es_cr/products/index.xml", descriptors.get(11).getUrl());
    }

    private DescriptorMergeStrategyResolver createStrategyResolver(DescriptorMergeStrategy defaultMergeStrategy) {
        InheritLevelsMergeStrategy inheritLevelsMergeStrategy = new InheritLevelsMergeStrategy();
        inheritLevelsMergeStrategy.setLevelDescriptorFileName(LEVEL_DESCRIPTOR_FILENAME);

        Map<String, DescriptorMergeStrategy> mappings = new LinkedHashMap<>(2);
        mappings.put("/site/website/products/index.xml", inheritLevelsMergeStrategy);
        mappings.put(".*", defaultMergeStrategy);

        UrlPatternMergeStrategyResolver strategyResolver = new UrlPatternMergeStrategyResolver();
        strategyResolver.setUrlPatternToStrategyMappings(mappings);

        return strategyResolver;
    }

    private CandidateTargetedUrlsResolver createCandidateUrlsResolver() {
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

    private void setUpCurrentSiteContext() {
        HierarchicalConfiguration config = mock(HierarchicalConfiguration.class);
        when(config.getStringArray(ROOT_FOLDERS_CONFIG_KEY)).thenReturn(ROOT_FOLDERS);
        when(config.getStringArray(AVAILABLE_TARGET_IDS_CONFIG_KEY)).thenReturn(AVAILABLE_TARGET_IDS);
        when(config.getString(FALLBACK_ID_CONFIG_KEY)).thenReturn(FALLBACK_TARGET_ID);

        SiteContext siteContext = new SiteContext();
        siteContext.setSiteName(SITE_NAME);
        siteContext.setConfig(config);

        SiteContext.setCurrent(siteContext);
    }
    
    private void clearCurrentSiteContext() {
        SiteContext.clear();
    }

}
