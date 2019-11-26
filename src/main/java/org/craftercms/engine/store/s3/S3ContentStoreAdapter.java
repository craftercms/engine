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
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.craftercms.core.exception.AuthenticationException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.core.exception.RootFolderNotFoundException;
import org.craftercms.core.exception.StoreException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.File;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.store.AbstractCachedFileBasedContentStoreAdapter;
import org.craftercms.engine.store.s3.util.S3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Implementation of {@link org.craftercms.core.store.ContentStoreAdapter} to read files from AWS S3.
 * @author joseross
 */
public class S3ContentStoreAdapter extends AbstractCachedFileBasedContentStoreAdapter implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(S3ContentStoreAdapter.class);

    public static final String DELIMITER = "/";

    protected S3ClientBuilder clientBuilder;
    protected AmazonS3 client;

    @Required
    public void setClientBuilder(final S3ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        client = clientBuilder.getClient();
    }

    public void destroy() {
        client.shutdown();
    }

    protected boolean isResultEmpty(ListObjectsV2Result result) {
        return (result.getCommonPrefixes() == null || result.getCommonPrefixes().isEmpty())
            && (result.getObjectSummaries() == null || result.getObjectSummaries().isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context createContext(final String id, final String rootFolderPath, final boolean mergingOn,
                                 final boolean cacheOn, final int maxAllowedItemsInCache,
                                 final boolean ignoreHiddenFiles)
        throws RootFolderNotFoundException, StoreException, AuthenticationException {

        AmazonS3URI uri = new AmazonS3URI(StringUtils.removeEnd(rootFolderPath, DELIMITER));

        ListObjectsV2Request request = new ListObjectsV2Request()
                                            .withBucketName(uri.getBucket())
                                            .withPrefix(uri.getKey())
                                            .withDelimiter(DELIMITER);
        ListObjectsV2Result result = client.listObjectsV2(request);

        if(isResultEmpty(result)) {
            throw new RootFolderNotFoundException("Root folder " + rootFolderPath + " not found");
        }

        return new S3Context(id, this, rootFolderPath, mergingOn, cacheOn, maxAllowedItemsInCache,
                             ignoreHiddenFiles, uri);
    }

    @Override
    protected Content getContent(Context context, CachingOptions cachingOptions,
                                 File file) throws InvalidContextException, StoreException {
        S3Context s3Context = (S3Context) context;
        String key = ((S3File) file).getKey();

        logger.debug("Getting content for key {}", key);

        try {
            GetObjectRequest request = new GetObjectRequest(s3Context.getBucket(), key);
            S3Object object = client.getObject(request);

            return new S3Content(object);
        } catch (AmazonS3Exception e) {
            if(e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new StoreException("No item found for key " + key);
            } else {
                throw new StoreException("Error getting item for key " + key, e);
            }
        }
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
        String key = StringUtils.appendIfMissing(s3Context.getKey(), path);

        logger.debug("Getting file for key {}", key);

        if (StringUtils.isEmpty(FilenameUtils.getExtension(key))) {
            // If it is a folder, check if there are objects with the prefix
            try {
                ListObjectsV2Request request = new ListObjectsV2Request()
                                                    .withBucketName(s3Context.getBucket())
                                                    .withPrefix(key)
                                                    .withDelimiter(DELIMITER);
                ListObjectsV2Result result = client.listObjectsV2(request);
                if (!isResultEmpty(result)) {
                    return new S3Prefix(StringUtils.appendIfMissing(key, DELIMITER));
                }
            } catch (AmazonS3Exception e) {
                if(e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    logger.debug("No object found for key {}", key);
                } else {
                    throw new StoreException("Error listing objects for key " + key, e);
                }
            }
        } else {
            // If it is a file, check if the key exist
            try {
                if (client.doesObjectExist(s3Context.getBucket(), key)) {
                    return new S3File(key);
                } else {
                    logger.debug("No object found for key {}", key);
                }
            } catch (AmazonS3Exception e) {
                throw new StoreException("Error checking if object for key " + key + " exists", e);
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

        if (!(dir instanceof S3Prefix)) {
            throw new StoreException("Can't get children for file " + dir);
        }

        S3Context s3Context = (S3Context) context;
        S3Prefix s3Prefix = (S3Prefix) dir;

        logger.debug("Getting children for key {}", s3Prefix.getPrefix());

        List<File> children = new CachingAwareList<>();
        ListObjectsV2Request request = new ListObjectsV2Request()
                                            .withBucketName(s3Context.getBucket())
                                            .withPrefix(s3Prefix.getPrefix())
                                            .withDelimiter(DELIMITER);
        ListObjectsV2Result result;

        do {
            result = client.listObjectsV2(request);
            if (isResultEmpty(result)) {
                return null;
            } else {
                result.getCommonPrefixes().stream()
                      .filter(p -> !context.ignoreHiddenFiles() || !isHidden(p))
                      .forEach(p -> children.add(new S3Prefix(StringUtils.appendIfMissing(p, DELIMITER))));

                result.getObjectSummaries().stream()
                      .filter(s-> !context.ignoreHiddenFiles() || !isHidden(s.getKey()))
                      .forEach(s -> children.add(new S3File(s.getKey())));
            }

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final Context context) throws StoreException, AuthenticationException {
        S3Context s3Context = (S3Context) context;
        ListObjectsV2Request request = new ListObjectsV2Request()
                                            .withBucketName(s3Context.getBucket())
                                            .withPrefix(s3Context.getKey())
                                            .withDelimiter(DELIMITER);
        ListObjectsV2Result result = client.listObjectsV2(request);

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

}
