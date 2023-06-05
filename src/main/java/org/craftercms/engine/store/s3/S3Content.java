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

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
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

    private final Supplier<S3Object> objectSupplier;

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
     * @param supplier S3Object Supplier to get the actual content
     */
    public S3Content(ObjectMetadata objectMetadata, boolean shouldCache, Supplier<S3Object> supplier) {
        this.lastModified = objectMetadata.getLastModified().getTime();
        this.length = objectMetadata.getContentLength();
        this.objectSupplier = supplier;
        if (shouldCache) {
            cacheContent();
        }
    }

    private void cacheContent() {
        S3Object s3Object = this.objectSupplier.get();
        content = new byte[(int) length];

        try (InputStream is = s3Object.getObjectContent()) {
            IOUtils.readFully(is, content);
        } catch (Exception e) {
            throw new StoreException(format("Error reading S3 item %s", s3Object), e);
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
        S3Object s3Object = this.objectSupplier.get();
        return s3Object.getObjectContent();
    }

}
