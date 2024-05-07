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

/**
 * Represents the metadata of an S3 object.
 */
public class S3ObjectMetadata {

    protected long lastModified;

    protected long contentLength;

    protected String bucketName;

    protected String keyName;

    public S3ObjectMetadata(long lastModified, long contentLength, String bucketName, String keyName) {
        this.lastModified = lastModified;
        this.contentLength = contentLength;
        this.bucketName = bucketName;
        this.keyName = keyName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getKeyName() {
        return keyName;
    }
}
