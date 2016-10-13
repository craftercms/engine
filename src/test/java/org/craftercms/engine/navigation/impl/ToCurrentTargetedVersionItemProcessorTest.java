package org.craftercms.engine.navigation.impl;

import org.apache.commons.configuration.XMLConfiguration;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.service.context.SiteContext;
import org.junit.Before;
import org.junit.Test;

import static org.craftercms.engine.properties.SiteProperties.TARGETING_ENABLED_CONFIG_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by alfonsovasquez on 8/10/16.
 */
public class ToCurrentTargetedVersionItemProcessorTest {

    private static final String TRANSFORMER_NAME = "toCurrentTargetedUrl";
    private static final String INDEX_EN_URL = "/site/website/index_en.xml";
    private static final String INDEX_EN_US_URL = "/site/website/index_en_US.xml";
    private static final String INDEX_FR_URL = "/site/website/index_fr.xml";

    private ToCurrentTargetedVersionItemProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new ToCurrentTargetedVersionItemProcessor();
        processor.setToCurrentTargetedUrlTransformerName(TRANSFORMER_NAME);
        processor.setUrlTransformationEngine(createUrlTransformationEngine());
        processor.setStoreService(createContentStoreService());

        setUpCurrentConfig();
    }

    @Test
    public void testProcess() throws Exception {
        Item item = new Item();
        item.setFolder(false);
        item.setUrl(INDEX_FR_URL);

        Item processedItem = processor.process(null, null, item);
        assertEquals(INDEX_EN_URL, processedItem.getUrl());
    }

    private UrlTransformationEngine createUrlTransformationEngine() {
        UrlTransformationEngine transformationEngine = mock(UrlTransformationEngine.class);
        when(transformationEngine.transformUrl(null, null, TRANSFORMER_NAME, INDEX_FR_URL)).thenReturn(INDEX_EN_US_URL);

        return transformationEngine;
    }

    private ContentStoreService createContentStoreService() {
        Item item = new Item();
        item.setUrl(INDEX_EN_URL);

        ContentStoreService storeService = mock(ContentStoreService.class);
        when(storeService.findItem(null, null, INDEX_EN_US_URL, null)).thenReturn(item);

        return storeService;
    }

    private void setUpCurrentConfig() {
        XMLConfiguration config = mock(XMLConfiguration.class);
        when(config.getBoolean(TARGETING_ENABLED_CONFIG_KEY, false)).thenReturn(true);

        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getSiteName()).thenReturn("test");
        when(siteContext.getConfig()).thenReturn(config);

        SiteContext.setCurrent(siteContext);
    }

}
