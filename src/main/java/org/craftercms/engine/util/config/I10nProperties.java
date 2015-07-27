package org.craftercms.engine.util.config;

import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.util.ConfigUtils;

/**
 * Easy accesor methods for i10n configuration properties.
 *
 * @author avasquez
 */
public class I10nProperties {

    private I10nProperties() {
    }

    public static boolean localizationEnabled() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(ConfigUtils.I10N_ENABLED_CONFIG_KEY);
        } else {
            return false;
        }
    }

    public static boolean forceCurrentLocale() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(ConfigUtils.I10N_FORCE_CURRENT_LOCALE_CONFIG_KEY);
        } else {
            return false;
        }
    }

    public static  String[] getLocalizedPaths() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getStringArray(ConfigUtils.I10N_LOCALIZED_PATHS_CONFIG_KEY);
        } else {
            return null;
        }
    }

    public static Locale getDefaultLocale() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            String localeStr = config.getString(ConfigUtils.I10N_DEFAULT_LOCALE_CONFIG_KEY);
            if (StringUtils.isNotEmpty(localeStr)) {
                return LocaleUtils.toLocale(localeStr);
            }
        }

        return null;
    }

    public static  boolean mergeFolders() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(ConfigUtils.I10N_MERGE_FOLDERS_CONFIG_KEY);
        } else {
            return false;
        }
    }
}
