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

import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TargetedUrlByFolderStrategy}.
 *
 * @author avasquez
 */
public class TargetedUrlByFolderStrategyTest {

    private static final List<String> AVAILABLE_TARGET_IDS = Arrays.asList("ame", "ame_lat", "ame_lat_cr");
    private static final String DEFAULT_TARGET_ID = "ame";
    private static final String CURRENT_TARGET_ID = "ame_lat_cr";
    private static final String NON_TARGETED_URL = "/products/index.xml";
    private static final String TARGETED_URL1 = "/ame_lat_cr/products/index.xml";
    private static final String TARGETED_URL2 = NON_TARGETED_URL;

    private TargetedUrlByFolderStrategy targetedUrlStrategy;
    @Mock
    private TargetIdManager targetIdManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(targetIdManager.getFallbackTargetId()).thenReturn(DEFAULT_TARGET_ID);
        when(targetIdManager.getCurrentTargetId()).thenReturn(CURRENT_TARGET_ID);
        when(targetIdManager.getAvailableTargetIds()).thenReturn(AVAILABLE_TARGET_IDS);

        targetedUrlStrategy = new TargetedUrlByFolderStrategy();
        targetedUrlStrategy.setTargetIdManager(targetIdManager);
    }

    @Test
    public void testToTargetUrl() throws Exception {
        String targetedUrl = targetedUrlStrategy.toTargetedUrl(NON_TARGETED_URL, false);

        assertEquals(TARGETED_URL1, targetedUrl);
    }

    @Test
    public void testToTargetUrlWithNoCurrentTargetId() throws Exception {
        when(targetIdManager.getCurrentTargetId()).thenReturn(null);

        String targetedUrl = targetedUrlStrategy.toTargetedUrl(NON_TARGETED_URL, false);

        assertEquals(TARGETED_URL2, targetedUrl);
    }

    @Test
    public void testParseUrl() throws Exception {
        TargetedUrlComponents urlComp = targetedUrlStrategy.parseTargetedUrl(TARGETED_URL1);

        assertEquals(null, urlComp.getPrefix());
        assertEquals("ame_lat_cr", urlComp.getTargetId());
        assertEquals("/products/index.xml", urlComp.getSuffix());
    }

    @Test
    public void testBuildUrl() throws Exception {
        String targetedUrl = targetedUrlStrategy.buildTargetedUrl(null, "ame_lat_cr", "/products/index.xml");

        assertEquals(TARGETED_URL1, targetedUrl);
    }

}
