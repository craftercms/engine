package org.craftercms.engine.util;

import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.engine.util.config.TargetingProperties;

/**
 * Created by alfonsovasquez on 21/8/15.
 */
public class TargetingUtils {

    private TargetingUtils() {
    }

    public static String getMatchingRootFolder(String targetedUrl) {
        String[] targetedRootFolders = TargetingProperties.getRootFolders();
        if (ArrayUtils.isNotEmpty(targetedRootFolders)) {
            for (String targetedRootFolder : targetedRootFolders) {
                if (targetedUrl.startsWith(targetedRootFolder)) {
                    return targetedRootFolder;
                }
            }
        }

        return null;
    }

}
