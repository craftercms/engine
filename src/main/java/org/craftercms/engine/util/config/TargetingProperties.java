package org.craftercms.engine.util.config;

import org.apache.commons.configuration.Configuration;
import org.craftercms.engine.util.ConfigUtils;

/**
 * Created by alfonsovasquez on 13/8/15.
 */
public class TargetingProperties {

    public static final String TARGETING_ENABLED_CONFIG_KEY = "targeting.enabled";
    public static final String AVAILABLE_TARGET_IDS_CONFIG_KEY = "targeting.availableTargetIds";
    public static final String FALLBACK_ID_CONFIG_KEY = "targeting.fallbackTargetId";
    public static final String ROOT_FOLDERS_CONFIG_KEY = "targeting.rootFolders";
    public static final String MERGE_FOLDERS_CONFIG_KEY = "targeting.mergeFolders";

    public static boolean isTargetingEnabled() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(TARGETING_ENABLED_CONFIG_KEY, false);
        } else {
            return false;
        }
    }

    public static String[] getAvailableTargetIds() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getStringArray(AVAILABLE_TARGET_IDS_CONFIG_KEY);
        } else {
            return null;
        }
    }

    public static String getFallbackTargetId() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(FALLBACK_ID_CONFIG_KEY);
        }

        return null;
    }

    public static String[] getRootFolders() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getStringArray(ROOT_FOLDERS_CONFIG_KEY);
        } else {
            return null;
        }
    }

    public static boolean isMergeFolders() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(MERGE_FOLDERS_CONFIG_KEY, false);
        } else {
            return false;
        }
    }

}
