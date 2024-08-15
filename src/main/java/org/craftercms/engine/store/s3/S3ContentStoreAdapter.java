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

package org.craftercms.engine.store.s3;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.core.exception.AuthenticationException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.core.exception.RootFolderNotFoundException;
import org.craftercms.core.exception.StoreException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.File;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.exception.s3.S3BucketNotConfiguredException;
import org.craftercms.engine.store.AbstractCachedFileBasedContentStoreAdapter;
import org.craftercms.engine.store.s3.util.S3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.Validator;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Implementation of {@link org.craftercms.core.store.ContentStoreAdapter} to read files from AWS S3.
 * @author joseross
 */
public class S3ContentStoreAdapter extends AbstractCachedFileBasedContentStoreAdapter implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(S3ContentStoreAdapter.class);

    public static final String DELIMITER = "/";

    protected final S3ClientBuilder clientBuilder;
    protected S3Client client;
    protected final int contentMaxLength;
    protected final String[] cacheAllowedPaths;

    @ConstructorProperties({"pathValidator", "descriptorFileExtension", "metadataFileExtension", "cacheTemplate",
            "clientBuilder", "contentMaxLength", "cacheAllowedPaths"})
    public S3ContentStoreAdapter(Validator pathValidator, String descriptorFileExtension,
                                 String metadataFileExtension, CacheTemplate cacheTemplate,
                                 final S3ClientBuilder clientBuilder, final int contentMaxLength, final String[] cacheAllowedPaths) {
        super(pathValidator, descriptorFileExtension, metadataFileExtension, cacheTemplate);
        this.clientBuilder = clientBuilder;
        this.contentMaxLength = contentMaxLength;
        this.cacheAllowedPaths = cacheAllowedPaths;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        client = clientBuilder.getClient();
    }

    public void destroy() {
        client.close();
    }

    /**
     * Check if the result of listing S3 object response is empty
     * @param result instance of {@link ListObjectsV2Response}
     * @return true if the result is empty, false otherwise
     */
    protected boolean isResultEmpty(ListObjectsV2Response result) {
        return (!result.hasCommonPrefixes() || result.commonPrefixes().isEmpty())
            && (!result.hasContents() || result.contents().isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context createContext(final String id, final String rootFolderPath, final boolean mergingOn,
                                 final boolean cacheOn, final int maxAllowedItemsInCache,
                                 final boolean ignoreHiddenFiles, Map<String, String> configurationVariables)
        throws RootFolderNotFoundException, StoreException, AuthenticationException {

        S3Uri uri = client.utilities().parseUri(URI.create(removeEnd(rootFolderPath, DELIMITER)));
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(uri.bucket().orElseThrow(S3BucketNotConfiguredException::new))
                .prefix(uri.key().orElse(""))
                .delimiter(DELIMITER).build();
        ListObjectsV2Response result = client.listObjectsV2(request);

        if (isResultEmpty(result)) {
            throw new RootFolderNotFoundException(format("Root folder '%s' not found", rootFolderPath));
        }

        return new S3Context(id, this, rootFolderPath, mergingOn, cacheOn, maxAllowedItemsInCache,
                             ignoreHiddenFiles, uri, configurationVariables);
    }

    @Override
    protected Content getContent(Context context, CachingOptions cachingOptions,
                                 File file) throws InvalidContextException, StoreException {
        S3Object s3Object = (S3Object) file;

        logger.debug("Getting content for {}", s3Object);

        return s3Object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File doFindFile(Context context, String path) throws InvalidContextException, StoreException {
        if (context.ignoreHiddenFiles() && isHidden(path)) {
            return null;
        }

        S3Context s3Context = (S3Context) context;
        String bucketName = s3Context.getBucket();
        String key = stripStart(appendIfMissing(s3Context.getKey(), path), DELIMITER);

        logger.debug("Getting file for s3://{}/{}", bucketName, key);

        if (isEmpty(FilenameUtils.getExtension(key))) {
            // If it is a folder, check if there are objects with the prefix
            try {
                ListObjectsV2Request request = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(appendIfMissing(key, DELIMITER))
                        .delimiter(DELIMITER)
                        .build();
                ListObjectsV2Response result = client.listObjectsV2(request);
                if (!isResultEmpty(result)) {
                    return new S3Prefix(bucketName, key);
                }
            } catch (S3Exception e) {
                if (e.statusCode() == HttpStatus.SC_NOT_FOUND) {
                    logger.debug("No object found for s3://{}/{}", bucketName, key);
                } else {
                    throw new StoreException(format("Error listing objects for s3://%s/%s", bucketName, key), e);
                }
            }
        } else {
            // If it is a file, get metadata and content for the file
            try {
                return getObject(s3Context, client, bucketName, key);
            } catch (S3Exception e) {
                throw new StoreException(format("Error checking if object for s3://%s/%s", bucketName, key), e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<File> doGetChildren(Context context, File dir)
        throws InvalidContextException, StoreException {

        if (!(dir instanceof S3Prefix prefix)) {
            throw new StoreException(format("Can't get children for file '%s'", dir));
        }

        S3Context s3Context = (S3Context) context;
        String bucketName = s3Context.getBucket();

        logger.debug("Getting children for s3://{}/{}", bucketName, prefix);

        List<File> children = new CachingAwareList<>();
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix.getPrefix())
                .delimiter(DELIMITER)
                .build();
        ListObjectsV2Iterable result = client.listObjectsV2Paginator(request);
        for (ListObjectsV2Response page : result) {
            page.commonPrefixes().stream()
                    .filter(p -> !context.ignoreHiddenFiles() || !isHidden(p.prefix()))
                    .forEach(p -> children.add(new S3Prefix(bucketName, p.prefix())));

            page.contents().stream()
                    .filter(s-> !context.ignoreHiddenFiles() || !isHidden(s.key()))
                    .forEach(s -> children.add(new S3File(bucketName, s.key())));
        }

        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Context context) throws StoreException, AuthenticationException {
        S3Context s3Context = (S3Context) context;
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Context.getBucket())
                .prefix(s3Context.getKey())
                .delimiter(DELIMITER)
                .build();
        ListObjectsV2Response result = client.listObjectsV2(request);

        return !isResultEmpty(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroyContext(final Context context) throws StoreException, AuthenticationException {
        // Nothing to do ...
    }

    private boolean isHidden(final String path) {
        return FilenameUtils.getName(path).startsWith(".");
    }


    /**
     * Get S3 object metadata and content
     * @param context the S3 context
     * @param client instance of {@link S3Client}
     * @param bucket bucket name
     * @param key key name
     * @return the S3 object, metadata and content included
     */
    private S3Object getObject(S3Context context, S3Client client, String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> objectIS = client.getObject(getObjectRequest)) {
            GetObjectResponse objectResp = objectIS.response();
            long lastModified = objectResp.lastModified().toEpochMilli();
            long contentLength = objectResp.contentLength();
            Supplier<InputStream> contentSupplier;

            if (shouldCache(context, key, contentLength)) {
                byte[] content = IOUtils.toByteArray(objectIS, contentLength);
                contentSupplier = () -> new ByteArrayInputStream(content);
            } else {
                objectIS.abort();
                contentSupplier = () -> client.getObject(getObjectRequest);
            }

            return new S3Object(bucket, key, lastModified, contentLength, contentSupplier);
        } catch (NoSuchKeyException e) {
            logger.debug("No object found for s3://{}/{}", bucket, key);

            return null;
        } catch (Exception e) {
            throw new StoreException(format("Error getting S3 object s3://%s/%s", bucket, key), e);
        }
    }

    /**
     * Indicates if the content should be cached in memory.
     * Content is cached if path matches the 'cacheAllowedPaths` and
     * the content length is not greater than contentMaxLength
     * @param context the S3 context
     * @param key the S3 object key
     * @param contentLength the S3 object content length
     * @return true if the content should be cached in memory, false otherwise
     */
    private boolean shouldCache(S3Context context, String key, long contentLength) {
        String folderPrefix = context.getKey();
        String path = substringAfter(stripStart(key, DELIMITER), stripStart(folderPrefix, DELIMITER));

        if (!RegexUtils.matchesAny(path, cacheAllowedPaths)) {
            return false;
        }
        return contentLength <= contentMaxLength;
    }

}
