package org.craftercms.engine.navigation.impl;

import org.craftercms.core.exception.ItemProcessingException;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.properties.SiteProperties;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 8/10/16.
 */
public class ToCurrentTargetedVersionItemProcessor implements ItemProcessor {

    protected String toCurrentTargetedUrlTransformerName;
    protected UrlTransformationEngine urlTransformationEngine;
    protected ContentStoreService storeService;
    protected TargetIdManager targetIdManager;

    @Required
    public void setToCurrentTargetedUrlTransformerName(String toCurrentTargetedUrlTransformerName) {
        this.toCurrentTargetedUrlTransformerName = toCurrentTargetedUrlTransformerName;
    }

    @Required
    public void setUrlTransformationEngine(UrlTransformationEngine urlTransformationEngine) {
        this.urlTransformationEngine = urlTransformationEngine;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setTargetIdManager(TargetIdManager targetIdManager) {
        this.targetIdManager = targetIdManager;
    }

    @Override
    public Item process(Context context, CachingOptions cachingOptions, Item item) throws ItemProcessingException {
        // Don't do this for folders, since for them this will be done by the FolderToIndexItemProcessor
        if (!item.isFolder() && SiteProperties.isTargetingEnabled()) {
            String url = item.getUrl();
            String targetedUrl = urlTransformationEngine.transformUrl(context, cachingOptions,
                                                                      toCurrentTargetedUrlTransformerName, url);
            Item targetedItem = storeService.findItem(context, cachingOptions, targetedUrl, null);

            if (targetedItem != null) {
                return targetedItem;
            }
        }

        return item;
    }

    @Override
    public String toString() {
        // The current target ID is added because the toString() method is used for item caching, and the result of the
        // folderToIndexFileTransformer varies by target ID
        return "ToCurrentTargetedVersionItemProcessor{" +
               "currentTargetId='" + targetIdManager.getCurrentTargetId() + '\'' +
               ", toCurrentTargetedUrlTransformerName='" + toCurrentTargetedUrlTransformerName + '\'' +
               ", urlTransformationEngine=" + urlTransformationEngine +
               ", storeService=" + storeService +
               '}';
    }

}
