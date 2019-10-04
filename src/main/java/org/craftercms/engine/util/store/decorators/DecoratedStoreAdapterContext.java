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
package org.craftercms.engine.util.store.decorators;

import org.craftercms.core.service.Context;
import org.craftercms.core.store.ContentStoreAdapter;

/**
 * {@link Context} wrapper used by {@link ContentStoreAdapterDecorator}.
 *
 * @author avasquez
 * @since 3.1.4
 */
public class DecoratedStoreAdapterContext implements Context {

    protected Context actualContext;
    protected ContentStoreAdapter decoratedStoreAdapter;

    public DecoratedStoreAdapterContext(Context actualContext, ContentStoreAdapter decoratedStoreAdapter) {
        this.actualContext = actualContext;
        this.decoratedStoreAdapter = decoratedStoreAdapter;
    }

    public Context getActualContext() {
        return actualContext;
    }

    @Override
    public String getId() {
        return actualContext.getId();
    }

    @Override
    public long getCacheVersion() {
        return actualContext.getCacheVersion();
    }

    @Override
    public void setCacheVersion(long cacheVersion) {
        actualContext.setCacheVersion(cacheVersion);
    }

    @Override
    public String getCacheScope() {
        return actualContext.getCacheScope();
    }

    @Override
    public ContentStoreAdapter getStoreAdapter() {
        return decoratedStoreAdapter;
    }

    @Override
    public boolean isMergingOn() {
        return actualContext.isMergingOn();
    }

    @Override
    public boolean isCacheOn() {
        return actualContext.isCacheOn();
    }

    @Override
    public int getMaxAllowedItemsInCache() {
        return actualContext.getMaxAllowedItemsInCache();
    }

    @Override
    public boolean ignoreHiddenFiles() {
        return actualContext.ignoreHiddenFiles();
    }

    @Override
    public Context clone() {
        return new DecoratedStoreAdapterContext(actualContext.clone(), decoratedStoreAdapter);
    }

    @Override
    public String toString() {
        return actualContext.toString();
    }

}
