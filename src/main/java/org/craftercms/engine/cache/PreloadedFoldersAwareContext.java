package org.craftercms.engine.cache;

import org.craftercms.core.service.Context;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.engine.util.store.decorators.DecoratedStoreAdapterContext;

import java.util.Collections;
import java.util.List;

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
