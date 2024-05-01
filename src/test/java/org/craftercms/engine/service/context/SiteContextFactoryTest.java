/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.service.context;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.engine.macro.MacroResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SiteContextFactoryTest {

    private static final String SITE_NAME_VARIABLE = "siteName";
    private static final String SITE_ID_VARIABLE = "siteId";
    private static final String SITE_NAME = "my-test-site";

    @Mock
    MacroResolver macroResolver;
    @Mock
    ContentStoreService contentStoreService;
    @Mock
    ObjectFactory<FreeMarkerConfig> freeMarkerConfigurationFactory;
    @InjectMocks
    SiteContextFactory siteContextFactory;

    @Before
    public void setUp() throws Exception {
        siteContextFactory.setConfigPaths(new String[]{});
        siteContextFactory.setApplicationContextPaths(new String[]{});
        siteContextFactory.setUrlRewriteConfPaths(new String[]{});
        siteContextFactory.setProxyConfigPaths(new String[]{});
        siteContextFactory.setTranslationConfigPaths(new String[]{});
        when(macroResolver.resolveMacros(any(), any())).thenReturn("");
    }

    @Test
    public void testCreateDefaultConfigVariablesContext() {
        siteContextFactory.createContext(SITE_NAME);
        verify(contentStoreService).getContext(any(), any(), any(), anyBoolean(), anyBoolean(), anyInt(), anyBoolean(), argThat(variables ->
                SITE_NAME.equals(variables.get(SITE_NAME_VARIABLE))
        ));
    }
}
