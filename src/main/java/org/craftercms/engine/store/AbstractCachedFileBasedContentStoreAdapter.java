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
package org.craftercms.engine.store;

import org.craftercms.commons.lang.Callback;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.core.exception.StoreException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.AbstractFileBasedContentStoreAdapter;
import org.craftercms.core.store.impl.File;
import org.craftercms.core.util.cache.impl.CachingAwareList;

import java.util.List;

/**
 * {@link AbstractFileBasedContentStoreAdapter} to provide extra caching for {@link File}s. Useful for adapters that
 * connect to remote stores, like S3.
 *
 * @author avasquez.
 */
public abstract class AbstractCachedFileBasedContentStoreAdapter extends AbstractFileBasedContentStoreAdapter {

    public static final String CONST_KEY_ELEM_FILE= "fileBasedContentStoreAdapter.file";
    public static final String CONST_KEY_ELEM_CHILDREN = "fileBasedContentStoreAdapter.children";

    @Override
    protected File findFile(Context context, CachingOptions cachingOptions,
                            String path) throws InvalidContextException, StoreException {
        final CachingOptions actualCachingOptions = cachingOptions != null? cachingOptions: defaultCachingOptions;

        return cacheTemplate.getObject(context, actualCachingOptions, new Callback<File>() {

            @Override
            public File execute() {
                return doFindFile(context, path);
            }

            @Override
            public String toString() {
                return String.format(AbstractCachedFileBasedContentStoreAdapter.this.getClass().getName() +
                                     ".findFile(%s, %s)", context, path);
            }

        }, context, path, CONST_KEY_ELEM_FILE);
    }

    @Override
    protected List<File> getChildren(Context context, CachingOptions cachingOptions,
                                     File dir) throws InvalidContextException, StoreException {
        final CachingOptions actualCachingOptions = cachingOptions != null? cachingOptions: defaultCachingOptions;

        return cacheTemplate.getObject(context, actualCachingOptions, new Callback<List<File>>() {

            @Override
            public List<File> execute() {
                List<File> children = doGetChildren(context, dir);
                if (children != null) {
                    if (children instanceof CachingAwareList) {
                        return children;
                    } else {
                        return new CachingAwareList<>(children);
                    }
                } else {
                    return null;
                }
            }

            @Override
            public String toString() {
                return String.format(AbstractCachedFileBasedContentStoreAdapter.this.getClass().getName() +
                                     ".getChildren(%s, %s)", context, dir);
            }

        }, context, dir, CONST_KEY_ELEM_CHILDREN);
    }

    protected abstract File doFindFile(Context context, String path) throws InvalidContextException, StoreException;

    protected abstract List<File> doGetChildren(Context context, File dir) throws InvalidContextException, StoreException;

}
