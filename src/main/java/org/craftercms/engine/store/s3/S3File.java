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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.StoreException;
import org.craftercms.core.store.impl.File;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Implementations of {@link File} for AWS S3 items.
 * @author joseross
 */
public class S3File implements File {

    /**
     * The name of the file.
     */
    protected String name;

    /**
     * The path of the file.
     */
    protected String path;

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

    public S3File(final S3Object s3Object) {
        name = FilenameUtils.getName(s3Object.getKey());
        path = FilenameUtils.getPath(s3Object.getKey());
        lastModified = s3Object.getObjectMetadata().getLastModified().getTime();
        length = s3Object.getObjectMetadata().getContentLength();
        content = new byte[(int)length];
        try(InputStream is = s3Object.getObjectContent()) {
            IOUtils.readFully(is, content);
        } catch (Exception e) {
            throw new StoreException("Error reading S3 item " + s3Object, e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
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
        return new ByteArrayInputStream(content);
    }

    @Override
    public String toString() {
        return "S3File{" + "name='" + name + '\'' + ", path='" + path + '\'' + ", lastModified=" + lastModified + ", "
            + "length=" + length + '}';
    }

}
