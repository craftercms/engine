package org.craftercms.engine.util.store.decorators;

import org.craftercms.core.service.Context;
import org.craftercms.core.store.ContentStoreAdapter;

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
