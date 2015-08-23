package org.craftercms.engine.targeting.impl;

import java.util.Arrays;
import java.util.List;

import org.craftercms.engine.targeting.TargetIdResolver;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by alfonsovasquez on 17/8/15.
 */
public class TargetedUrlByFileStrategyTest {

    private static final List<String> AVAILABLE_TARGET_IDS = Arrays.asList("ame", "ame_lat", "ame_lat_cr");
    private static final String DEFAULT_TARGET_ID = "ame";
    private static final String CURRENT_TARGET_ID = "ame_lat_cr";
    private static final String NON_TARGETED_URL = "/products/index.xml";
    private static final String TARGETED_URL = "/products/index_ame_lat_cr.xml";

    private TargetedUrlByFileStrategy targetedUrlByFileStrategy;
    @Mock
    private TargetIdResolver targetIdResolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(targetIdResolver.getFallbackTargetId()).thenReturn(DEFAULT_TARGET_ID);
        when(targetIdResolver.getCurrentTargetId()).thenReturn(CURRENT_TARGET_ID);
        when(targetIdResolver.getAvailableTargetIds()).thenReturn(AVAILABLE_TARGET_IDS);

        targetedUrlByFileStrategy = new TargetedUrlByFileStrategy();
        targetedUrlByFileStrategy.setTargetIdResolver(targetIdResolver);
    }

    @Test
    public void testAsTargetUrl() throws Exception {
        String targetedUrl = targetedUrlByFileStrategy.toTargetedUrl(NON_TARGETED_URL);

        assertEquals(TARGETED_URL, targetedUrl);
    }

    @Test
    public void testParseUrl() throws Exception {
        TargetedUrlComponents urlComp = targetedUrlByFileStrategy.parseTargetedUrl(TARGETED_URL);

        assertEquals("/products/index", urlComp.getPrefix());
        assertEquals("ame_lat_cr", urlComp.getTargetId());
        assertEquals(".xml", urlComp.getSuffix());
    }

    @Test
    public void testBuildUrl() throws Exception {
        String targetedUrl = targetedUrlByFileStrategy.buildTargetedUrl("/products/index", "ame_lat_cr", ".xml");

        assertEquals(TARGETED_URL, targetedUrl);
    }

}
