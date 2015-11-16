/*
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
package org.craftercms.engine.util.config;

import org.apache.commons.configuration.Configuration;
import org.craftercms.engine.util.ConfigUtils;

/**
 * Site configuration propeties related to content targeting.
 *
 * @author avasquez
 */
public class TargetingProperties {

    public static final String TARGETING_ENABLED_CONFIG_KEY = "targeting.enabled";
    public static final String AVAILABLE_TARGET_IDS_CONFIG_KEY = "targeting.availableTargetIds";
    public static final String FALLBACK_ID_CONFIG_KEY = "targeting.fallbackTargetId";
    public static final String ROOT_FOLDERS_CONFIG_KEY = "targeting.rootFolders";
    public static final String EXCLUDE_PATTERNS_CONFIG_KEY = "targeting.excludePatterns";
    public static final String MERGE_FOLDERS_CONFIG_KEY = "targeting.mergeFolders";
    public static final String REDIRECT_TO_TARGETED_URL_CONFIG_KEY = "targeting.redirectToTargetedUrl";

    /**
     * Returns trues if targeting is enabled.
     */
    public static boolean isTargetingEnabled() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(TARGETING_ENABLED_CONFIG_KEY, false);
        } else {
            return false;
        }
    }

    /**
     * Returns the list of available target IDs.
     */
    public static String[] getAvailableTargetIds() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getStringArray(AVAILABLE_TARGET_IDS_CONFIG_KEY);
        } else {
            return null;
        }
    }

    /**
     * Returns the fallback target ID. The fallback target ID is used in case none of the resolved candidate targeted
     * URLs map to existing content.
     */
    public static String getFallbackTargetId() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(FALLBACK_ID_CONFIG_KEY);
        }

        return null;
    }

    /**
     * Returns the folders that will be handled for targeted content.
     */
    public static String[] getRootFolders() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getStringArray(ROOT_FOLDERS_CONFIG_KEY);
        } else {
            return null;
        }
    }

    /**
     * Returns the patterns that a path might match if it should be excluded
     */
    public static String[] getExcludePatterns() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getStringArray(EXCLUDE_PATTERNS_CONFIG_KEY);
        } else {
            return null;
        }
    }

    /**
     * Returns true if the sub items of folders with the same family of target IDs should be merged (e.g. "en_US" and
     * "en" are of the same family).
     */
    public static boolean isMergeFolders() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(MERGE_FOLDERS_CONFIG_KEY, false);
        } else {
            return false;
        }
    }

    /**
     * Returns true if the request should be redirected when the targeted URL is different from the current URL.
     */
    public static boolean getRedirectToTargetedUrlConfigKey() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(REDIRECT_TO_TARGETED_URL_CONFIG_KEY, false);
        } else {
            return false;
        }
    }

}
