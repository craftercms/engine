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

import org.craftercms.core.service.CacheService;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.engine.util.store.decorators.DecoratedStoreAdapterContext;

import java.util.Collections;
import java.util.List;

/**
 * Extension of {@link DecoratedStoreAdapterContext} that keeps up a list of the preloaded folders in the cache
 * of the context.
 *
 * @author avasquez
 * @since 3.1.4
 */
class PreloadedFoldersAwareContext extends DecoratedStoreAdapterContext {

    public static final String PRELOADED_FOLDERS_CACHE_KEY = "cache.warmUp.preloadedFolders";

    protected CacheService cacheService;

    public PreloadedFoldersAwareContext(Context actualContext, ContentStoreAdapter decoratedStoreAdapter,
                                        CacheService cacheService) {
        super(actualContext, decoratedStoreAdapter);
        this.cacheService = cacheService;
    }

    @SuppressWarnings("unchecked")
    public List<PreloadedFolder> getPreloadedFolders() {
        List<PreloadedFolder> folders = (List<PreloadedFolder>) cacheService.get(this, PRELOADED_FOLDERS_CACHE_KEY);
        if (folders != null) {
            return folders;
        } else {
            return Collections.emptyList();
        }
    }

    public void setPreloadedFolders(List<PreloadedFolder> preloadedFolders) {
        cacheService.put(this, PRELOADED_FOLDERS_CACHE_KEY, preloadedFolders);
    }

    @Override
    public Context clone() {
        return new PreloadedFoldersAwareContext(actualContext.clone(), decoratedStoreAdapter, cacheService);
    }

}
