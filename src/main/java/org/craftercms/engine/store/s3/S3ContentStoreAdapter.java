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

package org.craftercms.engine.store.s3;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.craftercms.core.exception.AuthenticationException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.core.exception.RootFolderNotFoundException;
import org.craftercms.core.exception.StoreException;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.AbstractFileBasedContentStoreAdapter;
import org.craftercms.core.store.impl.File;
import org.craftercms.engine.store.s3.util.S3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Implementation of {@link org.craftercms.core.store.ContentStoreAdapter} to read files from AWS S3.
 * @author joseross
 */
public class S3ContentStoreAdapter extends AbstractFileBasedContentStoreAdapter {

    private static final Logger logger =
        LoggerFactory.getLogger(S3ContentStoreAdapter.class);

    public static final String DELIMITER = "/";

    protected S3ClientBuilder clientBuilder;

    @Required
    public void setClientBuilder(final S3ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    protected boolean isResultEmpty(ListObjectsV2Result result) {
        return (result.getCommonPrefixes() == null || result.getCommonPrefixes().isEmpty())
            && (result.getObjectSummaries() == null || result.getObjectSummaries().isEmpty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context createContext(final String id, final String storeServerUrl, final String username,
                                 final String password, final String rootFolderPath, final boolean mergingOn,
                                 final boolean cacheOn, final int maxAllowedItemsInCache,
                                 final boolean ignoreHiddenFiles)
        throws RootFolderNotFoundException, StoreException, AuthenticationException {

        AmazonS3URI uri = new AmazonS3URI(StringUtils.removeEnd(rootFolderPath, DELIMITER));

        ListObjectsV2Request request = new ListObjectsV2Request()
                                            .withBucketName(uri.getBucket())
                                            .withPrefix(uri.getKey())
                                            .withDelimiter(DELIMITER);
        ListObjectsV2Result result = clientBuilder.getClient().listObjectsV2(request);

        if(isResultEmpty(result)) {
            throw new RootFolderNotFoundException("Root folder " + rootFolderPath + " not found");
        }

        return new S3Context(id, this, storeServerUrl, rootFolderPath, mergingOn, cacheOn, maxAllowedItemsInCache,
            ignoreHiddenFiles, uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected File findFile(final Context context, final String path) throws InvalidContextException, StoreException {
        S3Context s3Context = (S3Context) context;
        String key = StringUtils.appendIfMissing(s3Context.getKey(), path);

        logger.debug("Getting item for key {}", key);
        AmazonS3 client = clientBuilder.getClient();
        if(StringUtils.isEmpty(FilenameUtils.getExtension(key))) {
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
                    logger.debug("No item found for key {}", key);
                } else {
                    logger.error("Error getting item for key " + key, e);
                    throw new StoreException("Error getting item for key " + key, e);
                }
            }
        } else {
            // If it is a file, check if the key exist
            try {
                GetObjectRequest request = new GetObjectRequest(s3Context.getBucket(), key);
                S3Object object = client.getObject(request);
                return new S3File(object);
            } catch (AmazonS3Exception e) {
                if(e.getStatusCode() == 404) {
                    logger.debug("No item found for key {}", key);
                } else {
                    logger.error("Error getting item for key " + key, e);
                    throw new StoreException("Error getting item for key " + key, e);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<File> getChildren(final Context context, final File dir)
        throws InvalidContextException, StoreException {

        if(!(dir instanceof S3Prefix)) {
            throw new StoreException("Can't get children for file " + dir);
        }

        S3Context s3Context = (S3Context) context;
        S3Prefix s3Prefix = (S3Prefix) dir;

        logger.debug("Getting children for key {}", s3Prefix.getPrefix());

        List<File> children = new LinkedList<>();
        AmazonS3 client = clientBuilder.getClient();

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
                result.getCommonPrefixes().forEach(prefix ->
                    children.add(new S3Prefix(StringUtils.appendIfMissing(prefix, DELIMITER))));
                result.getObjectSummaries().forEach(summary ->
                    children.add(new S3File(client.getObject(s3Context.getBucket(), summary.getKey()))));
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
        ListObjectsV2Result result = clientBuilder.getClient().listObjectsV2(request);

        return !isResultEmpty(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroyContext(final Context context) throws StoreException, AuthenticationException {
        // Nothing to do ...
    }

}
