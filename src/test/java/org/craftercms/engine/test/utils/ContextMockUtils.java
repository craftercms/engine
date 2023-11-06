/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.test.utils;

import org.craftercms.core.service.Context;
import org.craftercms.core.store.ContentStoreAdapter;

public class ContextMockUtils {
    public static Context createContext() {
        return new Context() {
            @Override
            public String getId() {
                return null;
            }

            @Override
            public long getCacheVersion() {
                return 0;
            }

            @Override
            public void setCacheVersion(long cacheVersion) {
                // Nothing
            }

            @Override
            public String getCacheScope() {
                return null;
            }

            @Override
            public ContentStoreAdapter getStoreAdapter() {
                return null;
            }

            @Override
            public boolean isMergingOn() {
                return false;
            }

            @Override
            public boolean isCacheOn() {
                return false;
            }

            @Override
            public int getMaxAllowedItemsInCache() {
                return 0;
            }

            @Override
            public boolean ignoreHiddenFiles() {
                return false;
            }

            @Override
            public Context clone() {
                return null;
            }
        };
    }
}
