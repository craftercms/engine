package org.craftercms.engine.targeting.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.targeting.CandidateTargetIdsResolver;
import org.craftercms.engine.targeting.TargetIdResolver;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.craftercms.engine.util.config.TargetingProperties.ROOT_FOLDERS_CONFIG_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by alfonsovasquez on 18/8/15.
 */
public class CandidateTargetedUrlsResolverImplTest {

    private static final String[] ROOT_FOLDERS = { "/site/website" };
    private static final String FALLBACK_TARGET_ID = "";
    private static final String CURRENT_TARGET_ID = "es_CR";
    private static final List<String> CANDIDATE_TARGET_IDS = Arrays.asList("es_CR", "es");
    private static final String TARGETED_URL = "/products/index_es_CR.xml";
    private static final String TARGETED_FULL_URL = ROOT_FOLDERS[0] + TARGETED_URL;

    private CandidateTargetedUrlsResolverImpl candidateUrlsResolver;

    @Before
    public void setUp() throws Exception {
        candidateUrlsResolver = new CandidateTargetedUrlsResolverImpl();
        candidateUrlsResolver.setTargetIdResolver(createTargetIdResolver());
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
        List<String> urls = candidateUrlsResolver.getUrls(TARGETED_FULL_URL);

        assertNotNull(urls);
        assertEquals(2, urls.size());
        assertEquals("/site/website/products/index_es_CR.xml", urls.get(0));
        assertEquals("/site/website/products/index_es.xml", urls.get(1));
    }

    private TargetIdResolver createTargetIdResolver() {
        TargetIdResolver targetIdResolver = mock(TargetIdResolver.class);

        when(targetIdResolver.getFallbackTargetId()).thenReturn(FALLBACK_TARGET_ID);

        return targetIdResolver;
    }

    private TargetedUrlStrategy createTargetedUrlStrategy() {
        TargetedUrlStrategy urlStrategy = mock(TargetedUrlStrategy.class);

        TargetedUrlComponents urlComp = new TargetedUrlComponents();
        urlComp.setPrefix("/products/index");
        urlComp.setTargetId(CURRENT_TARGET_ID);
        urlComp.setSuffix(".xml");

        when(urlStrategy.parseTargetedUrl(TARGETED_URL)).thenReturn(urlComp);
        doAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();

                return "" + args[0] + (args[1] != null? "_" + args[1] : "") + args[2];
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
        context.setConfig(config);

        SiteContext.setCurrent(context);
    }
    
    private void clearCurrentSiteContext() {
        SiteContext.clear();
    }

}
