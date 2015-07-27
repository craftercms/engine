package org.craftercms.engine.util;

import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.service.context.SiteContext;

/**
 * Configuration related utility methods.
 *
 * @author avasquez
 */
public class ConfigUtils {

    public static final String I10N_ENABLED_CONFIG_KEY = "i10n.enabled";
    public static final String I10N_FORCE_CURRENT_LOCALE_CONFIG_KEY = "i10n.forceCurrentLocale";
    public static final String I10N_LOCALIZED_PATHS_CONFIG_KEY = "i10n.localizedPaths";
    public static final String I10N_DEFAULT_LOCALE_CONFIG_KEY = "i10n.defaultLocale";
    public static final String I10N_MERGE_FOLDERS_CONFIG_KEY = "i10n.mergeFolders";

    private ConfigUtils() {
    }

    /**
     * Returns the configuration from the current site context.
     */
    public static HierarchicalConfiguration getCurrentConfig() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return siteContext.getConfig();
        } else {
            return null;
        }
    }

}
