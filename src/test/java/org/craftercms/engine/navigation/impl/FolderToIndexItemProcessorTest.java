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
package org.craftercms.engine.navigation.impl;

import java.util.Collections;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.targeting.impl.LocaleTargetIdManager;
import org.dom4j.Document;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by alfonsovasquez on 9/10/16.
 */
public class FolderToIndexItemProcessorTest {

    private static final String TRANSFORMER_NAME = "folderToIndexUrl";
    private static final String WEBSITE_FOLDER_URL = "/site/website";
    private static final String INDEX_EN_URL = "/site/website/index_en.xml";
    private static final String XML = "<page><file-name>index_en.xml</file-name></page>";

    private FolderToIndexItemProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new FolderToIndexItemProcessor(TRANSFORMER_NAME, createUrlTransformationEngine(),
                createContentStoreService(), new LocaleTargetIdManager());
    }

    @Test
    public void testProcess() throws Exception {
        Item item = new Item();
        item.setFolder(true);
        item.setUrl(WEBSITE_FOLDER_URL);

        Item processedItem = processor.process(null, null, item);
        assertEquals(WEBSITE_FOLDER_URL, processedItem.getUrl());
        assertEquals(INDEX_EN_URL, processedItem.getDescriptorUrl());
        assertEquals(XML, processedItem.getDescriptorDom().asXML());
    }

    private UrlTransformationEngine createUrlTransformationEngine() {
        UrlTransformationEngine transformationEngine = mock(UrlTransformationEngine.class);
        when(transformationEngine.transformUrl(null, null, TRANSFORMER_NAME, WEBSITE_FOLDER_URL))
            .thenReturn(INDEX_EN_URL);

        return transformationEngine;
    }

    private ContentStoreService createContentStoreService() {
        Document dom = mock(Document.class);
        when(dom.asXML()).thenReturn(XML);

        Item item = new Item();
        item.setDescriptorUrl(INDEX_EN_URL);
        item.setDescriptorDom(dom);
        item.setKey(INDEX_EN_URL);

        ContentStoreService storeService = mock(ContentStoreService.class);
        when(storeService.findItem(null, null, INDEX_EN_URL, null)).thenReturn(item);

        return storeService;
    }

}
