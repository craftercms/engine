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
        SiteContext context = SiteContext.getCurrent();
        if (context != null) {
            return context.getConfig();
        } else {
            return null;
        }
    }

}
