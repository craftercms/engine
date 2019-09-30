package org.craftercms.engine.util.store.decorators;

import org.craftercms.core.store.ContentStoreAdapter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.List;

public class ContentStoreAdapterDecoratorChainFactoryBean extends AbstractFactoryBean<ContentStoreAdapter> {

    private ContentStoreAdapter storeAdapter;
    private List<ContentStoreAdapterDecorator> decorators;

    @Required
    public void setStoreAdapter(ContentStoreAdapter storeAdapter) {
        this.storeAdapter = storeAdapter;
    }

    @Required
    public void setDecorators(List<ContentStoreAdapterDecorator> decorators) {
        this.decorators = decorators;
    }

    @Override
    public Class<?> getObjectType() {
        return ContentStoreAdapter.class;
    }

    @Override
    protected ContentStoreAdapter createInstance() {
        ContentStoreAdapter currentAdapter = storeAdapter;

        for (ContentStoreAdapterDecorator decorator : decorators) {
            decorator.setActualStoreAdapter(currentAdapter);
            currentAdapter = decorator;
        }

        return currentAdapter;
    }

}
