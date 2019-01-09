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

package org.craftercms.engine.util.groovy;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.impl.GroovyScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by alfonsovasquez on 1/8/16.
 */
public class Dom4jExtensionTest {

    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                                      "<root>" +
                                      "<collection>" +
                                      "<item>Item #1</item>" +
                                      "<item>Item #2</item>" +
                                      "</collection>" +
                                      "</root>";

    private ScriptFactory scriptFactory;

    @Before
    public void setUp() throws Exception {
        SiteContext siteContext = createSiteContext(createContentStoreService());
        Map<String, Object> globalVars = Collections.emptyMap();

        scriptFactory = createScriptFactory(siteContext, globalVars);
    }

    @Test
    public void testExtension() throws Exception {
        SAXReader reader = new SAXReader();
        reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Map<String, Object> vars = new HashMap<>(1);
        vars.put("document", reader.read(new StringReader(XML)));

        Object result = scriptFactory.getScript("/scripts/testDom4jExtension.get.groovy").execute(vars);

        assertEquals("Item #2", result);
    }

    private ContentStoreService createContentStoreService() {
        ContentStoreService storeService = mock(ContentStoreService.class);
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);

        return storeService;
    }

    private SiteContext createSiteContext(ContentStoreService storeService) {
        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getSiteName()).thenReturn("default");
        when(siteContext.getStoreService()).thenReturn(storeService);

        return siteContext;
    }

    private ScriptFactory createScriptFactory(SiteContext siteContext, Map<String, Object> globalVars) {
        ContentStoreResourceConnector resourceConnector = new ContentStoreResourceConnector(siteContext);

        return new GroovyScriptFactory(resourceConnector, globalVars);
    }

}
