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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.StoreException;
import org.craftercms.core.service.Content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

import static java.lang.String.format;

/**
 * Represents an S3 object.
 *
 * @author jross
 * @author avasquez
 * @since 3.1.4
 */
public class S3Object extends S3File implements Content {

    /**
     * When the file was last modified.
     */
    protected long lastModified;

    /**
     * The content length.
     */
    protected long contentLength;

    /**
     * The actual content
     */
    protected Supplier<InputStream> contentSupplier;

    /**
     * Main constructor.
     *
     * @param bucketName the S3 bucket
     * @param key the S3 key
     * @param lastModified the last modified timestamp
     * @param contentLength the content size
     * @param contentSupplier an InputStream supplier for the content
     */
    public S3Object(String bucketName, String key, long lastModified, long contentLength,
                    Supplier<InputStream> contentSupplier) {
        super(bucketName, key);

        this.lastModified = lastModified;
        this.contentLength = contentLength;
        this.contentSupplier = contentSupplier;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public long getLength() {
        return contentLength;
    }

    @Override
    public InputStream getInputStream() {
        return this.contentSupplier.get();
    }

    @Override
    public String toString() {
        return "S3Object{" +
                "bucketName='" + bucketName + '\'' +
                ", key='" + key + '\'' +
                ", lastModified=" + lastModified +
                ", contentLength=" + FileUtils.byteCountToDisplaySize(contentLength) +
                '}';
    }
}
