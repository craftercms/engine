/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.store;

import org.craftercms.engine.exception.s3.S3BucketNotConfiguredException;
import org.craftercms.engine.store.s3.S3SiteListResolver;
import org.craftercms.engine.store.s3.util.S3ClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class S3SiteListResolverTest {
    private static final String SITE_NAME_MACRO_NAME = "siteName";
    private static final String SITE_NAME_MACRO_PLACEHOLDER = "{" + SITE_NAME_MACRO_NAME + "}";
    private static final String TEST_URI_ROOT = "s3://test-bucket-" + SITE_NAME_MACRO_PLACEHOLDER;
    private static final String TEST_URI_SUB_FOLDER = "s3://test-bucket/sites/" + SITE_NAME_MACRO_PLACEHOLDER;

    @Mock
    private S3ClientBuilder clientBuilderMock;

    @Mock
    private S3Client s3ClientMock;

    private static S3Client defaultClient;

    private static S3Utilities defaultUtilities;

    @Mock
    private static S3Utilities mockUtilities;

    private S3SiteListResolver resolver;

    @Before
    public void setup() {
        defaultClient = S3Client.builder()
                .credentialsProvider(dummyCreds())
                .region(Region.US_EAST_1)
                .build();
        defaultUtilities = defaultClient.utilities();
        when(clientBuilderMock.getClient()).thenReturn(s3ClientMock);
        when(s3ClientMock.utilities()).thenReturn(defaultUtilities);
    }

    @Test
    public void testGetSiteListFromBucketNames() {
        resolver = new S3SiteListResolver(TEST_URI_ROOT, clientBuilderMock);

        Bucket bucket1 = Bucket.builder().name("test-bucket-testSite").build();
        Bucket bucket2 = Bucket.builder().name("test-bucket/testSite").build();
        List<Bucket> buckets = Arrays.asList(bucket1, bucket2);

        when(s3ClientMock.listBuckets()).thenReturn(ListBucketsResponse.builder().buckets(buckets).build());

        Collection<String> siteList = resolver.getSiteList();

        assertEquals(1, siteList.size());
        assertEquals(List.of("testSite"), siteList);
    }

    @Test
    public void testGetSiteListFromBucketKeys() {
        resolver = new S3SiteListResolver(TEST_URI_SUB_FOLDER, clientBuilderMock);

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .commonPrefixes(CommonPrefix.builder().prefix("sites/site1/").build(),
                        CommonPrefix.builder().prefix("sites/site2/").build())
                .build();

        when(s3ClientMock.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        Collection<String> siteList = resolver.getSiteList();

        assertEquals(2, siteList.size());
        assertEquals(List.of("site1", "site2"), siteList);
    }

    @Test
    public void testNoBucketException() {
        when(s3ClientMock.utilities()).thenReturn(mockUtilities);
        when(mockUtilities.parseUri(any())).thenReturn(S3Uri.builder()
                .uri(URI.create("s3://sample-bucket/folder"))
                .build()); // empty bucket name
        resolver = new S3SiteListResolver(TEST_URI_SUB_FOLDER, clientBuilderMock);
        assertThrows(S3BucketNotConfiguredException.class, () -> {
            resolver.getSiteList();
        });
    }

    private static AwsCredentialsProvider dummyCreds() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create("akid", "skid"));
    }
}
