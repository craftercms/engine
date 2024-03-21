/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.store.decorators;

import org.craftercms.core.store.ContentStoreAdapter;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.List;

/**
 * Spring {@code FactoryBean} that is used to wrap a {@link ContentStoreAdapter} with decorators.
 *
 * @author avasquez
 * @since 3.1.4
 */
public class ContentStoreAdapterDecoratorChainFactoryBean extends AbstractFactoryBean<ContentStoreAdapter> {

    private ContentStoreAdapter storeAdapter;
    private List<ContentStoreAdapterDecorator> decorators;

    public ContentStoreAdapterDecoratorChainFactoryBean(ContentStoreAdapter storeAdapter, List<ContentStoreAdapterDecorator> decorators) {
        this.storeAdapter = storeAdapter;
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
