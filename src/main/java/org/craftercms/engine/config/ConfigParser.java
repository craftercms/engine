package org.craftercms.engine.config;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.craftercms.engine.exception.ConfigurationException;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Implementations parse a {@link org.apache.commons.configuration.Configuration} to create bean definitions for
 * a site.
 *
 * @author avasquez
 */
public interface ConfigParser {

    /**
     * Parses the specified configuration, creating any required bean definitions and adding them to the
     * registry.
     *
     * @param config                the configuration to read
     * @param applicationContext    the application context to add the bean definitions to
     */
    void parse(HierarchicalConfiguration config,
               GenericApplicationContext applicationContext) throws ConfigurationException;

}
