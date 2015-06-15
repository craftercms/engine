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
