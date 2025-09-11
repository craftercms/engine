/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import java.io.ByteArrayInputStream;

/**
 * Represents an S3 object that can be cached internal or in a distributed cache like Redis.
 *
 * @author avasquez
 * @since 4.5.0
 */
public class S3CachedObject extends S3Object {

	protected byte[] content;

	public S3CachedObject(String bucketName, String key, long lastModified, long contentLength, byte[] content) {
		super(bucketName, key, lastModified, contentLength, () -> new ByteArrayInputStream(content));
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

}
