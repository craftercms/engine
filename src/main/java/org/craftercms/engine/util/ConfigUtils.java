package org.craftercms.engine.util;

import org.apache.commons.configuration.XMLConfiguration;
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
    public static XMLConfiguration getCurrentConfig() {
        SiteContext context = SiteContext.getCurrent();
        if (context != null) {
            return context.getConfig();
        } else {
            return null;
        }
    }

}
