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

import org.craftercms.core.exception.ItemProcessingException;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.targeting.TargetIdManager;
import org.springframework.beans.factory.annotation.Required;

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

    @Required
    public void setFolderToIndexUrlTransformerName(String folderToIndexUrlTransformerName) {
        this.folderToIndexUrlTransformerName = folderToIndexUrlTransformerName;
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
