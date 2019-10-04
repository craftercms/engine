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

    protected List<PreloadedFolder> preloadedFolders;

    public PreloadedFoldersAwareContext(Context actualContext, ContentStoreAdapter decoratedStoreAdapter) {
        super(actualContext, decoratedStoreAdapter);
        this.preloadedFolders = Collections.emptyList();
    }

    public PreloadedFoldersAwareContext(Context actualContext, ContentStoreAdapter decoratedStoreAdapter,
                                        List<PreloadedFolder> preloadedFolders) {
        super(actualContext, decoratedStoreAdapter);
        this.preloadedFolders = preloadedFolders;
    }

    public List<PreloadedFolder> getPreloadedFolders() {
        return preloadedFolders;
    }

    public void setPreloadedFolders(List<PreloadedFolder> preloadedFolders) {
        this.preloadedFolders = preloadedFolders;
    }

    @Override
    public Context clone() {
        return new PreloadedFoldersAwareContext(actualContext.clone(), decoratedStoreAdapter, preloadedFolders);
    }

}
