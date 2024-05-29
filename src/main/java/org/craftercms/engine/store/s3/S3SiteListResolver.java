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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.engine.exception.s3.S3BucketNotConfiguredException;
import org.craftercms.engine.service.context.SiteContextFactory;
import org.craftercms.engine.service.context.SiteListResolver;
import org.craftercms.engine.store.s3.util.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.net.URI;
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
    protected S3Uri s3Uri;
    protected S3ClientBuilder clientBuilder;

    public S3SiteListResolver(String s3Uri, final S3ClientBuilder clientBuilder) {
        setSiteNameMacroName(SiteContextFactory.DEFAULT_SITE_NAME_MACRO_NAME);

        this.clientBuilder = clientBuilder;
        this.s3Uri = clientBuilder.getClient().utilities().parseUri(URI.create(HttpUtils.encodeUrlMacro(s3Uri)));
    }

    public void setSiteNameMacroName(String siteNameMacroName) {
        this.siteNameMacroPlaceholder = "{" + siteNameMacroName + "}";
    }

    @Override
    public Collection<String> getSiteList() {
        String bucketName = s3Uri.bucket().orElseThrow(() -> new S3BucketNotConfiguredException());
        if (bucketName.contains(siteNameMacroPlaceholder)) {
            String bucketNameRegex = bucketName.replace(siteNameMacroPlaceholder, "(.+)");
            return getSiteListFromBucketNames(bucketNameRegex);
        } else {
            String rootPrefix = StringUtils.substringBefore(s3Uri.key().orElse(""), siteNameMacroPlaceholder);
            return getSiteListFromBucketKeys(bucketName, rootPrefix);
        }
    }

    /**
     * Get site list of site from buckets match a regex
     * @param bucketNameRegex the regex to match sites
     * @return list of sites
     */
    protected Collection<String> getSiteListFromBucketNames(String bucketNameRegex) {
        List<String> siteNames = new ArrayList<>();
        S3Client client = clientBuilder.getClient();

        List<Bucket> buckets = client.listBuckets().buckets();
        if (CollectionUtils.isNotEmpty(buckets)) {
            for (Bucket bucket : buckets) {
                Matcher bucketNameMatcher = Pattern.compile(bucketNameRegex).matcher(bucket.name());
                if (bucketNameMatcher.matches()) {
                    siteNames.add(bucketNameMatcher.group(1));
                }
            }
        }

        return siteNames;
    }

    /**
     * Get sites list from a bucket with a root prefix
     * @param bucketName the bucket name
     * @param rootPrefix the root prefix
     * @return list of sites
     */
    protected Collection<String> getSiteListFromBucketKeys(String bucketName, String rootPrefix) {
        List<String> siteNames = new ArrayList<>();
        S3Client client = clientBuilder.getClient();

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(rootPrefix)
                .delimiter(DELIMITER)
                .build();

        ListObjectsV2Response result = client.listObjectsV2(request);
        if (CollectionUtils.isNotEmpty(result.commonPrefixes())) {
            result.commonPrefixes()
                  .stream()
                  .map(p -> StringUtils.stripEnd(StringUtils.removeStart(p.prefix(), rootPrefix), DELIMITER))
                  .forEach(siteNames::add);
        }

        return siteNames;
    }

}
