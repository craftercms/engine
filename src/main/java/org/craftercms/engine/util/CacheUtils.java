package org.craftercms.engine.util;

import org.craftercms.core.service.ContentStoreService;

import java.util.HashMap;
import java.util.Map;

public class CacheUtils {

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
