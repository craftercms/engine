package org.craftercms.engine.util.store.decorators;

import org.craftercms.core.store.ContentStoreAdapter;

public interface ContentStoreAdapterDecorator extends ContentStoreAdapter {

    void setActualStoreAdapter(ContentStoreAdapter actualStoreAdapter);

}
