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

package org.craftercms.engine.store.s3;

import org.craftercms.core.service.ContextImpl;
import org.craftercms.core.store.ContentStoreAdapter;
import software.amazon.awssdk.services.s3.S3Uri;

import java.util.Map;

/**
 * Implementation of {@link org.craftercms.core.service.Context} for AWS S3.
 *
 * @author joseross
 */
public class S3Context extends ContextImpl {

    /**
     * AWS S3 bucket uri to use as root folder for the site.
     */
    protected S3Uri rootFolderUri;

    public S3Context(final String id, final ContentStoreAdapter storeAdapter, final String rootFolderPath,
                     final boolean mergingOn, final boolean cacheOn, final int maxAllowedItemsInCache,
                     final boolean ignoreHiddenFiles, final S3Uri rootFolderUri,
                     final Map<String, String> configurationVariables) {
        super(id, storeAdapter, rootFolderPath, mergingOn, cacheOn, maxAllowedItemsInCache, ignoreHiddenFiles, configurationVariables);
        this.rootFolderUri = rootFolderUri;
    }

    /**
     * Returns the name of the bucket.
     */
    public String getBucket() {
        return rootFolderUri.bucket().orElseThrow();
    }

    /**
     * Returns the key of the folder.
     */
    public String getKey() {
        return rootFolderUri.key().orElse("");
    }

}
