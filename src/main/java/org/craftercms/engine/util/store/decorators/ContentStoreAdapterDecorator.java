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

import org.craftercms.core.store.ContentStoreAdapter;

/**
 * Represents a decorator to a {@link ContentStoreAdapter}.
 *
 * @author avasquez
 * @since 3.1.4
 */
public interface ContentStoreAdapterDecorator extends ContentStoreAdapter {

    /**
     * Sets the store adapter to be decorated
     *
     * @param actualStoreAdapter the actual store adapter
     */
    void setActualStoreAdapter(ContentStoreAdapter actualStoreAdapter);

}
