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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.XMLConfiguration;
import org.craftercms.core.service.Item;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.craftercms.engine.targeting.impl.TargetedUrlByFileStrategy;
import org.junit.Before;
import org.junit.Test;

import static org.craftercms.engine.properties.SiteProperties.DEFAULT_INDEX_FILE_NAME;
import static org.craftercms.engine.properties.SiteProperties.INDEX_FILE_NAME_CONFIG_KEY;
import static org.craftercms.engine.properties.SiteProperties.TARGETING_ENABLED_CONFIG_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by alfonsovasquez on 29/9/16.
 */
public class RejectIndexFilesItemFilterTest {

    private RejectIndexFilesItemFilter filter;

    @Before
    public void setUp() throws Exception {
        filter = createFilter(createTargetedUrlStrategy());

        setUpCurrentConfig();
    }

    @Test
    public void testFilter() throws Exception {
        List<Item> emptyItemList = Collections.emptyList();

        Item item = new Item();
        item.setName("index.xml");

        boolean accepted = filter.accepts(item, emptyItemList, emptyItemList, true);
        assertFalse(accepted);

        item.setName("index_en.xml");

        accepted = filter.accepts(item, emptyItemList, emptyItemList, true);
        assertFalse(accepted);

        item.setName("index_en_US.xml");

        accepted = filter.accepts(item, emptyItemList, emptyItemList, true);
        assertFalse(accepted);

        item.setName("about-us");

        accepted = filter.accepts(item, emptyItemList, emptyItemList, true);
        assertTrue(accepted);
    }

    private RejectIndexFilesItemFilter createFilter(TargetedUrlStrategy strategy) {
        RejectIndexFilesItemFilter filter = new RejectIndexFilesItemFilter();
        filter.setTargetedUrlStrategy(strategy);

        return filter;
    }

    private TargetedUrlStrategy createTargetedUrlStrategy() {
        TargetIdManager targetIdManager = mock(TargetIdManager.class);
        when(targetIdManager.getAvailableTargetIds()).thenReturn(Arrays.asList("en", "en_US"));

        TargetedUrlByFileStrategy strategy = new TargetedUrlByFileStrategy();
        strategy.setTargetIdManager(targetIdManager);

        return strategy;
    }

    private void setUpCurrentConfig() {
        XMLConfiguration config = mock(XMLConfiguration.class);
        when(config.getString(INDEX_FILE_NAME_CONFIG_KEY, DEFAULT_INDEX_FILE_NAME)).thenReturn(DEFAULT_INDEX_FILE_NAME);
        when(config.getBoolean(TARGETING_ENABLED_CONFIG_KEY, false)).thenReturn(true);

        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getSiteName()).thenReturn("test");
        when(siteContext.getConfig()).thenReturn(config);

        SiteContext.setCurrent(siteContext);
    }

}
