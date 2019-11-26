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
package org.craftercms.engine.cache;

import org.craftercms.core.exception.*;
import org.craftercms.core.service.*;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.util.ContentStoreUtils;
import org.craftercms.engine.util.store.decorators.ContentStoreAdapterDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.function.Supplier;

/**
 * {@link ContentStoreAdapterDecorator} that is aware of cache warming and uses the preloaded folders to check
 * if items exist before even going to the actual content store adapter, enhancing thus performance.
 *
 * @author avasquez
 * @since 3.1.3
 */
public class CacheWarmingAwareContentStoreAdapterDecorator implements ContentStoreAdapterDecorator {

    private static final Logger logger = LoggerFactory.getLogger(CacheWarmingAwareContentStoreAdapterDecorator.class);

    protected boolean warmUpEnabled;
    protected ContentStoreAdapter actualStoreAdapter;
    protected CacheService cacheService;

    @Required
    public void setWarmUpEnabled(boolean warmUpEnabled) {
        this.warmUpEnabled = warmUpEnabled;
    }

    @Override
    public void setActualStoreAdapter(ContentStoreAdapter actualStoreAdapter) {
        this.actualStoreAdapter = actualStoreAdapter;
    }

    @Required
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public Context createContext(String id, String rootFolderPath, boolean mergingOn, boolean cacheOn,
                                 int maxAllowedItemsInCache, boolean ignoreHiddenFiles)
            throws RootFolderNotFoundException, StoreException, AuthenticationException {
        Context context = actualStoreAdapter.createContext(id, rootFolderPath, mergingOn, cacheOn,
                                                           maxAllowedItemsInCache, ignoreHiddenFiles);
        if (warmUpEnabled) {
            return new PreloadedFoldersAwareContext(context, actualStoreAdapter, cacheService);
        } else {
            return context;
        }
    }

    @Override
    public boolean validate(Context context) throws StoreException, AuthenticationException {
        if (warmUpEnabled) {
            return actualStoreAdapter.validate(((PreloadedFoldersAwareContext) context).getActualContext());
        } else {
            return actualStoreAdapter.validate(context);
        }
    }

    @Override
    public void destroyContext(Context context) throws StoreException, AuthenticationException {
        if (warmUpEnabled) {
            actualStoreAdapter.destroyContext(((PreloadedFoldersAwareContext) context).getActualContext());
        } else {
            actualStoreAdapter.destroyContext(context);
        }
    }

    @Override
    public boolean exists(Context context, CachingOptions cachingOptions, String path)
            throws InvalidContextException, StoreException {
        return findItem(context, cachingOptions, path, false) != null;
    }

    @Override
    public Content findContent(Context context, CachingOptions cachingOptions, String path)
            throws InvalidContextException, StoreException {
        if (warmUpEnabled) {
            String normalizedPath = ContentStoreUtils.normalizePath(path);

            PreloadedFoldersAwareContext contextWrapper = (PreloadedFoldersAwareContext) context;
            Context actualContext = contextWrapper.getActualContext();

            return executeIfNotPreloadedOrIfExistsInPreloadedPaths(contextWrapper, normalizedPath, () ->
                    actualStoreAdapter.findContent(actualContext, cachingOptions, normalizedPath));
        } else {
            return actualStoreAdapter.findContent(context, cachingOptions, path);
        }
    }

    @Override
    public Item findItem(Context context, CachingOptions cachingOptions, String path, boolean withDescriptor)
            throws InvalidContextException, XmlFileParseException, StoreException {
        if (warmUpEnabled) {
            String normalizedPath = ContentStoreUtils.normalizePath(path);

            PreloadedFoldersAwareContext contextWrapper = (PreloadedFoldersAwareContext) context;
            Context actualContext = contextWrapper.getActualContext();

            return executeIfNotPreloadedOrIfExistsInPreloadedPaths(contextWrapper, normalizedPath, () ->
                    actualStoreAdapter.findItem(actualContext, cachingOptions, normalizedPath, withDescriptor));
        } else {
            return actualStoreAdapter.findItem(context, cachingOptions, path, withDescriptor);
        }
    }

    @Override
    public List<Item> findItems(Context context, CachingOptions cachingOptions, String path)
            throws InvalidContextException, XmlFileParseException, StoreException {
        if (warmUpEnabled) {
            String normalizedPath = ContentStoreUtils.normalizePath(path);

            PreloadedFoldersAwareContext contextWrapper = (PreloadedFoldersAwareContext) context;
            Context actualContext = contextWrapper.getActualContext();

            return executeIfNotPreloadedOrIfExistsInPreloadedPaths(contextWrapper, normalizedPath, () ->
                    actualStoreAdapter.findItems(actualContext, cachingOptions, normalizedPath));
        } else {
            return actualStoreAdapter.findItems(context, cachingOptions, path);
        }
    }

    protected <T> T executeIfNotPreloadedOrIfExistsInPreloadedPaths(PreloadedFoldersAwareContext contextWrapper,
                                                                    String path, Supplier<T> actualCall) {
        PreloadedFolder preloadedAncestor = findPreloadedAncestor(contextWrapper.getPreloadedFolders(), path);
        if (preloadedAncestor != null) {
            Boolean exists = preloadedAncestor.exists(path);
            // Don't proceed if path is preloaded and doesn't exist (null means the path's level wasn't preloaded)
            if (exists != null && !exists) {
                logger.debug("Path {} not found in preloaded descendants of {}", path, preloadedAncestor);

                return null;
            }
        }

        return actualCall.get();
    }

    protected PreloadedFolder findPreloadedAncestor(List<PreloadedFolder> preloadedFolders, String path) {
        return preloadedFolders.stream()
                               .filter(folder -> path.startsWith(folder.getPath()))
                               .findFirst()
                               .orElse(null);
    }

}
