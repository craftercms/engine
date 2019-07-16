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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.service.context.SiteContextFactory;
import org.craftercms.engine.service.context.SiteListResolver;
import org.craftercms.engine.store.s3.util.S3ClientBuilder;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.engine.store.s3.S3ContentStoreAdapter.DELIMITER;

/**
 * Implementation of {@link SiteListResolver} for AWS S3.
 * @author joseross
 */
public class S3SiteListResolver implements SiteListResolver {

    protected String siteNameMacroPlaceholder;
    protected AmazonS3URI s3Uri;
    protected S3ClientBuilder clientBuilder;

    public S3SiteListResolver() {
        setSiteNameMacroName(SiteContextFactory.DEFAULT_SITE_NAME_MACRO_NAME);
    }

    public void setSiteNameMacroName(String siteNameMacroName) {
        this.siteNameMacroPlaceholder = "{" + siteNameMacroName + "}";
    }

    @Required
    public void setS3Uri(String s3Uri) {
        this.s3Uri = new AmazonS3URI(s3Uri);
    }

    @Required
    public void setClientBuilder(final S3ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public Collection<String> getSiteList() {
        String bucketName = s3Uri.getBucket();
        if (bucketName.contains(siteNameMacroPlaceholder)) {
            String bucketNameRegex = bucketName.replace(siteNameMacroPlaceholder, "(.+)");
            return getSiteListFromBucketNames(bucketNameRegex);
        } else {
            String rootPrefix = StringUtils.substringBefore(s3Uri.getKey(), siteNameMacroPlaceholder);
            return getSiteListFromBucketKeys(bucketName, rootPrefix);
        }
    }

    protected Collection<String> getSiteListFromBucketNames(String bucketNameRegex) {
        List<String> siteNames = new ArrayList<>();
        AmazonS3 client = clientBuilder.getClient();

        List<Bucket> buckets = client.listBuckets();
        if (CollectionUtils.isNotEmpty(buckets)) {
            for (Bucket bucket : buckets) {
                Matcher bucketNameMatcher = Pattern.compile(bucketNameRegex).matcher(bucket.getName());
                if (bucketNameMatcher.matches()) {
                    siteNames.add(bucketNameMatcher.group(1));
                }
            }
        }

        return siteNames;
    }
    
    protected Collection<String> getSiteListFromBucketKeys(String bucketName, String rootPrefix) {
        List<String> siteNames = new ArrayList<>();
        AmazonS3 client = clientBuilder.getClient();

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(rootPrefix)
                .withDelimiter(DELIMITER);

        ListObjectsV2Result result = client.listObjectsV2(request);
        if(CollectionUtils.isNotEmpty(result.getCommonPrefixes())) {
            result.getCommonPrefixes()
                  .stream()
                  .map(prefix -> StringUtils.stripEnd(StringUtils.removeStart(prefix, rootPrefix), DELIMITER))
                  .forEach(siteNames::add);
        }

        return siteNames;
    }

}
