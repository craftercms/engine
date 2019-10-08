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

package org.craftercms.engine.store.s3;

import org.craftercms.core.service.ContextImpl;
import org.craftercms.core.store.ContentStoreAdapter;
import com.amazonaws.services.s3.AmazonS3URI;

/**
 * Implementation of {@link org.craftercms.core.service.Context} for AWS S3.
 *
 * @author joseross
 */
public class S3Context extends ContextImpl {

    /**
     * AWS S3 bucket uri to use as root folder for the site.
     */
    protected AmazonS3URI rootFolderUri;

    public S3Context(final String id, final ContentStoreAdapter storeAdapter, final String rootFolderPath,
                     final boolean mergingOn, final boolean cacheOn, final int maxAllowedItemsInCache,
                     final boolean ignoreHiddenFiles, final AmazonS3URI rootFolderUri) {
        super(id, storeAdapter, rootFolderPath, mergingOn, cacheOn, maxAllowedItemsInCache, ignoreHiddenFiles);
        this.rootFolderUri = rootFolderUri;
    }

    /**
     * Returns the name of the bucket.
     */
    public String getBucket() {
        return rootFolderUri.getBucket();
    }

    /**
     * Returns the key of the folder.
     */
    public String getKey() {
        return rootFolderUri.getKey();
    }

}
