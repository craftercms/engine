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
package org.craftercms.engine.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;

/**
 * Utility methods for a Crafter content store.
 *
 * @author avasquez
 */
public class ContentStoreUtils {

    private ContentStoreUtils() {
    }

    public static List<String> findChildrenUrl(ContentStoreService storeService, Context context, String folderUrl) {
        List<Item> children = storeService.findChildren(context, folderUrl);
        List<String> childrenUrl = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(children)) {
            for (Item child : children) {
                childrenUrl.add(child.getUrl());
            }
        }

        return childrenUrl;
    }

}
