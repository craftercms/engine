/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.plugin.impl;

import org.craftercms.commons.config.EncryptionAwareConfigurationReader;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.engine.service.context.SiteContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author joseross
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginServiceImplTest {

    public static final String PLUGIN_PATH = "org/craftercms/test/plugin";

    public static final String PLUGIN_CONFIG_PATH = "/config/plugins/" + PLUGIN_PATH + "/config.xml";

    public static final String PLUGIN_SCRIPT_URL = "/scripts/rest/plugins/" + PLUGIN_PATH + "/foo/hello.get.groovy";

    public static final String REGULAR_SCRIPT_URL = "/scripts/rest/hello.get.groovy";

    @Spy
    private SiteContext siteContext;

    @Mock
    private ContentStoreService contentStoreService;

    @Mock
    private EncryptionAwareConfigurationReader configurationReader;

    @Mock
    private Content content;

    @InjectMocks
    private PluginServiceImpl pluginService;

    @Before
    public void setUp() throws IOException {
        SiteContext.setCurrent(siteContext);

        when(contentStoreService.exists(any(), eq(PLUGIN_CONFIG_PATH))).thenReturn(true);
        when(contentStoreService.getContent(any(), eq(PLUGIN_CONFIG_PATH))).thenReturn(content);
    }

    @Test
    public void pluginUrlTest() {
        Map<String, Object> variables = new HashMap<>();
        pluginService.addPluginVariables(PLUGIN_SCRIPT_URL, variables::put);

        assertEquals(2, variables.size());
    }

    @Test
    public void regularUrlTest() {
        Map<String, Object> variables = new HashMap<>();
        pluginService.addPluginVariables(REGULAR_SCRIPT_URL, variables::put);

        assertEquals(0, variables.size());
    }

}
