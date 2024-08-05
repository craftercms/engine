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

package org.craftercms.engine.store.s3;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.store.impl.File;
import org.craftercms.core.util.cache.impl.AbstractCachingAwareObject;

import static org.craftercms.engine.store.s3.S3ContentStoreAdapter.DELIMITER;

/**
 * Implementation of {@link File} for AWS S3 prefixes (used as folders).
 * @author joseross
 */
public class S3Prefix extends AbstractCachingAwareObject implements File {

    /**
     * The bucket name.
     */
    protected String bucketName;
    /**
     * The full prefix.
     */
    protected String prefix;

    public S3Prefix(String bucketName, String prefix) {
        this.bucketName = bucketName;
        if (!prefix.equals(DELIMITER)) {
            prefix = StringUtils.stripStart(prefix, DELIMITER);
            prefix = StringUtils.appendIfMissing(prefix, DELIMITER);
        }

        this.prefix = prefix;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getName() {
        return FilenameUtils.getName(StringUtils.removeEnd(prefix, DELIMITER));
    }

    @Override
    public String getPath() {
        return prefix;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public String toString() {
        return "S3Prefix{" +
                "bucketName='" + bucketName + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }

}
