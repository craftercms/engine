/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.StoreException;
import org.craftercms.core.service.Content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

import static java.lang.String.format;

/**
 * Represents the content of an S3 object.
 *
 * @author jross
 * @author avasquez
 * @since 3.1.4
 */
public class S3Content implements Content {

    private final Supplier<InputStream> objectSupplier;

    /**
     * Name of the bucket
     */
    protected String bucketName;

    /**
     * The S3 object key
     */
    protected String keyName;

    /**
     * When the file was last modified.
     */
    protected long lastModified;

    /**
     * The length of the file.
     */
    protected long length;

    /**
     * The content of the file.
     */
    protected byte[] content;

    /**
     * @param objectMetadata S3 Object metadata
     * @param shouldCache indicates if the object content should be loaded and cached in memory.
     * @param supplier InputStream Supplier to get the actual content
     */
    public S3Content(S3ObjectMetadata objectMetadata, boolean shouldCache, Supplier<InputStream> supplier) {
        this.lastModified = objectMetadata.getLastModified();
        this.length = objectMetadata.getContentLength();
        this.bucketName = objectMetadata.getBucketName();
        this.keyName = objectMetadata.getKeyName();
        this.objectSupplier = supplier;
        if (shouldCache) {
            cacheContent();
        }
    }

    private void cacheContent() {
        content = new byte[(int) length];

        try (InputStream is = this.objectSupplier.get()) {
            IOUtils.readFully(is, content);
        } catch (Exception e) {
            throw new StoreException(format("Error reading S3 item %s:%s", bucketName, keyName), e);
        }
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public InputStream getInputStream() {
        if (content != null) {
            return new ByteArrayInputStream(content);
        }
        return this.objectSupplier.get();
    }

}
