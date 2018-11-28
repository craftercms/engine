/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All Rights Reserved.
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
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Utility class to build the AWS S3 client instances.
 * @author joseross
 */
public class S3ClientBuilder {

    /**
     * AWS S3 Region
     */
    protected String region;

    /**
     * AWS Access Key
     */
    protected String accessKey;

    /**
     * AWS Secret Key
     */
    protected String secretKey;

    public void setRegion(final String region) {
        this.region = region;
    }

    public void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Builds an AWS S3 client, if no values are provided the default client will be used.
     * @return AWS S3 client
     */
    public AmazonS3 getClient() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        if(StringUtils.isNotEmpty(region)) {
            builder.withRegion(region);
        }
        if(StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey)) {
            builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
        }
        return builder.build();
    }

}
