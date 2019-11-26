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

import org.apache.commons.io.FilenameUtils;
import org.craftercms.core.store.impl.File;
import org.craftercms.core.util.cache.impl.AbstractCachingAwareObject;

/**
 * Implementations of {@link File} for AWS S3 items.
 * @author joseross
 */
public class S3File extends AbstractCachingAwareObject implements File {

    /**
     * The S3 key.
     */
    protected String key;

    public S3File(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return FilenameUtils.getName(key);
    }

    @Override
    public String getPath() {
        return FilenameUtils.getPath(key);
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
    public String toString() {
        return "S3File{" +
               "key='" + key + '\'' +
               '}';
    }

}
