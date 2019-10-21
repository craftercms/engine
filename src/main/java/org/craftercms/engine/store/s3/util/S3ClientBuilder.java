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

package org.craftercms.engine.store.s3.util;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.config.profiles.aws.S3Profile;
import org.craftercms.commons.file.stores.S3Utils;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;

/**
 * Utility class to build the AWS S3 client instances.
 * @author joseross
 */
public class S3ClientBuilder {

    protected S3Profile profile;

    public S3ClientBuilder(String endpoint, String region, String accessKey, String secretKey) {
        profile = new S3Profile();
        profile.setEndpoint(endpoint);
        profile.setRegion(region);
        if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey)) {
            profile.setCredentialsProvider(
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
        }
    }

    /**
     * Builds an AWS S3 client, if no values are provided the default client will be used.
     * @return AWS S3 client
     */
    public AmazonS3 getClient() {
        return S3Utils.createClient(profile);
    }

}
