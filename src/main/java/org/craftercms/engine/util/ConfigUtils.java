package org.craftercms.engine.util;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.craftercms.engine.service.context.SiteContext;

/**
 * Configuration related utility methods.
 *
 * @author avasquez
 */
public class ConfigUtils {

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
