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

/**
 * {@link ItemProcessor} that modifies the a folder item to include the descriptor DOM of it's index file.
 *
 * @author avasquez
 */
public class FolderToIndexItemProcessor implements ItemProcessor {

    protected String folderToIndexUrlTransformerName;
    protected UrlTransformationEngine urlTransformationEngine;
    protected ContentStoreService storeService;
    protected TargetIdManager targetIdManager;

    public FolderToIndexItemProcessor(String folderToIndexUrlTransformerName, UrlTransformationEngine urlTransformationEngine,
                                      ContentStoreService storeService, TargetIdManager targetIdManager) {
        this.folderToIndexUrlTransformerName = folderToIndexUrlTransformerName;
        this.urlTransformationEngine = urlTransformationEngine;
        this.storeService = storeService;
        this.targetIdManager = targetIdManager;
    }

    @Override
    public Item process(Context context, CachingOptions cachingOptions, Item item) throws ItemProcessingException {
        if (item.isFolder()) {
            String folderUrl = item.getUrl();
            String indexUrl = urlTransformationEngine.transformUrl(context, cachingOptions,
                                                                   folderToIndexUrlTransformerName, folderUrl);
            Item indexItem = storeService.findItem(context, cachingOptions, indexUrl, null);

            if (indexItem != null) {
                Item newFolderItem = new Item(item);

                newFolderItem.setDescriptorUrl(indexItem.getDescriptorUrl());
                newFolderItem.setDescriptorDom(indexItem.getDescriptorDom());

                return newFolderItem;
            }
        }

        return item;
    }

    @Override
    public String toString() {
        // The current target ID is added because the toString() method is used for item caching, and the result of the
        // folderToIndexFileTransformer varies by target ID
        return "FolderToIndexItemProcessor{" +
               "currentTargetId='" + targetIdManager.getCurrentTargetId() + '\'' +
               ", folderToIndexUrlTransformerName='" + folderToIndexUrlTransformerName + '\'' +
               ", urlTransformationEngine=" + urlTransformationEngine +
               ", storeService=" + storeService +
               '}';
    }

}
