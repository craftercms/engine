package org.craftercms.engine.util;

import org.craftercms.core.service.ContentStoreService;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods of cache related operations.
 *
 * @author avasquez
 * @since 3.1.4
 */
public class CacheUtils {

    /**
     * Parses a list of preloaded folder paths and their depths, in the following format: {PATH}:{DEPTH}
     *
     * @param preloadFolders the list of folder paths to preload
     * @return a map with {@code key = preload path} and {@code value = depth}
     */
    public static Map<String, Integer> parsePreloadFoldersList(String[] preloadFolders) {
        Map<String, Integer> preloadFoldersMappings = new HashMap<>();

        for (String folder : preloadFolders) {
            String[] folderAndDepth = folder.split(":");
            if (folderAndDepth.length > 1) {
                preloadFoldersMappings.put(folderAndDepth[0], Integer.parseInt(folderAndDepth[1]));
            } else if (folderAndDepth.length == 1) {
                preloadFoldersMappings.put(folderAndDepth[0], ContentStoreService.UNLIMITED_TREE_DEPTH);
            }
        }

        return preloadFoldersMappings;
    }

}
