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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.service.context.FolderScanningSiteListResolver;
import org.craftercms.engine.service.context.SiteListResolver;
import org.craftercms.engine.store.s3.util.S3ClientBuilder;
import org.springframework.beans.factory.annotation.Required;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;

import static org.craftercms.engine.store.s3.S3ContentStoreAdapter.DELIMITER;

/**
 * Implementation of {@link SiteListResolver} for AWS S3.
 * @author joseross
 */
public class S3SiteListResolver extends FolderScanningSiteListResolver {

    protected S3ClientBuilder clientBuilder;

    @Required
    public void setClientBuilder(final S3ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getSiteList() {
        List<String> siteNames = new ArrayList<>();
        AmazonS3URI uri = new AmazonS3URI(sitesFolderPath);
        AmazonS3 client = clientBuilder.getClient();

        ListObjectsV2Request request = new ListObjectsV2Request()
                                            .withBucketName(uri.getBucket())
                                            .withPrefix(uri.getKey())
                                            .withDelimiter(DELIMITER);

        ListObjectsV2Result result = client.listObjectsV2(request);
        if(CollectionUtils.isNotEmpty(result.getCommonPrefixes())) {
            result.getCommonPrefixes()
                .stream()
                .map(p -> StringUtils.removeEnd(StringUtils.removeStart(p, uri.getKey()), DELIMITER))
                .forEach(siteNames::add);
        }

        return siteNames;
    }

}
