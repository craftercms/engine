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
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.targeting.CandidateTargetIdsResolver;
import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.craftercms.engine.properties.SiteProperties.ROOT_FOLDERS_CONFIG_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CandidateTargetedUrlsResolverImpl}.
 *
 * @author avasquez
 */
public class CandidateTargetedUrlsResolverImplTest {

    private static final String SITE_NAME = "test";
    private static final String[] ROOT_FOLDERS = { "/site/website" };
    private static final String FALLBACK_TARGET_ID = "";
    private static final String CURRENT_TARGET_ID = "es_cr";
    private static final List<String> CANDIDATE_TARGET_IDS = Arrays.asList("es_cr", "es", "");
    private static final String TARGETED_URL1 = "/products/index_es_cr.xml";
    private static final String TARGETED_URL2 = "/products/index.xml";
    private static final String TARGETED_FULL_URL1 = ROOT_FOLDERS[0] + TARGETED_URL1;
    private static final String TARGETED_FULL_URL2 = ROOT_FOLDERS[0] + TARGETED_URL2;

    private CandidateTargetedUrlsResolverImpl candidateUrlsResolver;

    @Before
    public void setUp() throws Exception {
        candidateUrlsResolver = new CandidateTargetedUrlsResolverImpl();
        candidateUrlsResolver.setTargetIdManager(createTargetIdManager());
        candidateUrlsResolver.setTargetedUrlStrategy(createTargetedUrlStrategy());
        candidateUrlsResolver.setCandidateTargetIdsResolver(createCandidateTargetIdsResolver());

        setUpCurrentSiteContext();
    }

    @After
    public void tearDown() throws Exception {
        clearCurrentSiteContext();
    }

    @Test
    public void testGetUrls() throws Exception {
        List<String> urls = candidateUrlsResolver.getUrls(TARGETED_FULL_URL1);

        assertNotNull(urls);
        assertEquals(3, urls.size());
        assertEquals("/site/website/products/index_es_cr.xml", urls.get(0));
        assertEquals("/site/website/products/index_es.xml", urls.get(1));
        assertEquals("/site/website/products/index.xml", urls.get(2));

        urls = candidateUrlsResolver.getUrls(TARGETED_FULL_URL2);

        assertNotNull(urls);
        assertEquals(1, urls.size());
        assertEquals("/site/website/products/index.xml", urls.get(0));
    }

    private TargetIdManager createTargetIdManager() {
        TargetIdManager targetIdManager = mock(TargetIdManager.class);

        when(targetIdManager.getFallbackTargetId()).thenReturn(FALLBACK_TARGET_ID);

        return targetIdManager;
    }

    private TargetedUrlStrategy createTargetedUrlStrategy() {
        TargetedUrlStrategy urlStrategy = mock(TargetedUrlStrategy.class);

        TargetedUrlComponents urlComp = new TargetedUrlComponents();
        urlComp.setPrefix("/products/index");
        urlComp.setTargetId(CURRENT_TARGET_ID);
        urlComp.setSuffix(".xml");

        when(urlStrategy.parseTargetedUrl(TARGETED_URL1)).thenReturn(urlComp);
        doAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String prefix = (String)args[0];
                String targetId = (String)args[1];
                String suffix = (String)args[2];

                return "" + prefix + (StringUtils.isNotEmpty(targetId)? "_" + targetId : "") + suffix;
            }

        }).when(urlStrategy).buildTargetedUrl(anyString(), anyString(), anyString());

        return urlStrategy;
    }

    private CandidateTargetIdsResolver createCandidateTargetIdsResolver() {
        CandidateTargetIdsResolver candidateTargetIdsResolver = mock(CandidateTargetIdsResolver.class);

        when(candidateTargetIdsResolver.getTargetIds(CURRENT_TARGET_ID,
                                                     FALLBACK_TARGET_ID)).thenReturn(CANDIDATE_TARGET_IDS);

        return candidateTargetIdsResolver;
    }

    private void setUpCurrentSiteContext() {
        HierarchicalConfiguration config = mock(HierarchicalConfiguration.class);
        when(config.getStringArray(ROOT_FOLDERS_CONFIG_KEY)).thenReturn(ROOT_FOLDERS);

        SiteContext context = new SiteContext();
        context.setSiteName(SITE_NAME);
        context.setConfig(config);

        SiteContext.setCurrent(context);
    }
    
    private void clearCurrentSiteContext() {
        SiteContext.clear();
    }

}
