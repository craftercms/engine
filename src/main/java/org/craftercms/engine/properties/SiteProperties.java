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
package org.craftercms.engine.properties;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.engine.util.ConfigUtils;

/**
 * Properties specific of the current site.
 *
 * @author avasquez
 */
public class SiteProperties {

    public static final String INDEX_FILE_NAME_CONFIG_KEY = "indexFileName";
    public static final String DISABLE_FULL_MODEL_TYPE_CONVERSION_CONFIG_KEY = "compatibility.disableFullModelTypeConversion";
    public static final String NAVIGATION_ADDITIONAL_FIELDS_CONFIG_KEY = "navigation.additionalFields";

    /*
     * Single Page Application properties
     */
    public static final String SPA_ENABLED_CONFIG_KEY = "spa.enabled";
    public static final String SPA_VIEW_NAME = "spa.viewName";

    /*
     * Targeting properties
     */
    public static final String TARGETING_ENABLED_CONFIG_KEY = "targeting.enabled";
    public static final String AVAILABLE_TARGET_IDS_CONFIG_KEY = "targeting.availableTargetIds";
    public static final String FALLBACK_ID_CONFIG_KEY = "targeting.fallbackTargetId";
    public static final String ROOT_FOLDERS_CONFIG_KEY = "targeting.rootFolders";
    public static final String EXCLUDE_PATTERNS_CONFIG_KEY = "targeting.excludePatterns";
    public static final String MERGE_FOLDERS_CONFIG_KEY = "targeting.mergeFolders";
    public static final String REDIRECT_TO_TARGETED_URL_CONFIG_KEY = "targeting.redirectToTargetedUrl";

    /*
     * Defaults
     */
    public static final String DEFAULT_INDEX_FILE_NAME = "index.xml";
    public static final String DEFAULT_SPA_VIEW_NAME = "/";

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
        } else {
            return null;
        }
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
    public static boolean isRedirectToTargetedUrl() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(REDIRECT_TO_TARGETED_URL_CONFIG_KEY, false);
        } else {
            return false;
        }
    }

    /**
     * Returns the name of a page's index file, or {@link #DEFAULT_INDEX_FILE_NAME} if not in configuration.
     */
    public static final String getIndexFileName() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(INDEX_FILE_NAME_CONFIG_KEY, DEFAULT_INDEX_FILE_NAME);
        } else {
            return DEFAULT_INDEX_FILE_NAME;
        }
    }

    /**
     * Returns true if full content model type conversion should be disabled.
     *
     * Up to and including version 2:
     * Crafter Engine, in the FreeMarker host only, converts model elements based on a suffix type hint, but only for the first level in
     * the model, and not for _dt. For example, for contentModel.myvalue_i Integer is returned, but for contentModel.repeater.myvalue_i
     * and contentModel.date_dt a String is returned. In the Groovy host no type of conversion was performed.
     *
     * In version 3 onwards, Crafter Engine converts elements with any suffix type hints (including _dt) at at any level in the content
     * model and for both Freemarker and Groovy hosts.
     */
    public static boolean isDisableFullModelTypeConversion() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(DISABLE_FULL_MODEL_TYPE_CONVERSION_CONFIG_KEY, false);
        } else {
            return false;
        }
    }

    /**
     * Returns the list of additional fields that navigation items should extract from the item descriptor.
     */
    public static String[] getNavigationAdditionalFields() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if(config != null && config.containsKey(NAVIGATION_ADDITIONAL_FIELDS_CONFIG_KEY)) {
            return config.getStringArray(NAVIGATION_ADDITIONAL_FIELDS_CONFIG_KEY);
        } else {
            return new String[] {};
        }
    }

    /**
     * Returns true if SPA (Single Page App) mode is enabled.
     */
    public static boolean isSpaEnabled() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(SPA_ENABLED_CONFIG_KEY, false);
        } else {
            return false;
        }
    }

    /**
     * Returns the view name for the SPA (Single Page Application). Current view names can be a page URL (like /)
     * or a template name (like /template/web/app.ftl). By default, if SPA is enabled and no view name config property
     * is found, / is returned.
     */
    public static String getSpaViewName() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(SPA_VIEW_NAME, DEFAULT_SPA_VIEW_NAME);
        } else {
            return DEFAULT_SPA_VIEW_NAME;
        }
    }

}
