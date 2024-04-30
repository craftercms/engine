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
package org.craftercms.engine.cache;

import org.craftercms.core.exception.*;
import org.craftercms.core.service.*;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.util.ContentStoreUtils;
import org.craftercms.engine.util.store.decorators.ContentStoreAdapterDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
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

    public CacheWarmingAwareContentStoreAdapterDecorator(boolean warmUpEnabled, CacheService cacheService) {
        this.warmUpEnabled = warmUpEnabled;
        this.cacheService = cacheService;
    }

    @Override
    public void setActualStoreAdapter(ContentStoreAdapter actualStoreAdapter) {
        this.actualStoreAdapter = actualStoreAdapter;
    }

    @Override
    public Context createContext(String id, String rootFolderPath, boolean mergingOn, boolean cacheOn,
                                 int maxAllowedItemsInCache, boolean ignoreHiddenFiles, Map<String, String> configurationVariables)
            throws RootFolderNotFoundException, StoreException, AuthenticationException {
        Context context = actualStoreAdapter.createContext(id, rootFolderPath, mergingOn, cacheOn,
                                                           maxAllowedItemsInCache, ignoreHiddenFiles, configurationVariables);
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
