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

import org.craftercms.core.exception.ItemProcessingException;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.properties.SiteProperties;

/**
 * {@link ItemProcessor} that converts the item to the current targeted item (e.g. if the current target ID is en_US, then index.xml
 * might be converted to index_en_US.xml, or to index_en.xml if it the former doesn't exist).
 *
 * <b>WARNING:</b> this processor will probably generate the same item for different URLs, so it's good to use the
 * {@link RejectDuplicatesItemFilter} in conjunction with this processor.
 */
public class ToCurrentTargetedVersionItemProcessor implements ItemProcessor {

    protected String toCurrentTargetedUrlTransformerName;
    protected UrlTransformationEngine urlTransformationEngine;
    protected ContentStoreService storeService;
    protected TargetIdManager targetIdManager;

    public ToCurrentTargetedVersionItemProcessor(String toCurrentTargetedUrlTransformerName,
                                                 UrlTransformationEngine urlTransformationEngine,
                                                 ContentStoreService storeService, TargetIdManager targetIdManager) {
        this.toCurrentTargetedUrlTransformerName = toCurrentTargetedUrlTransformerName;
        this.urlTransformationEngine = urlTransformationEngine;
        this.storeService = storeService;
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
