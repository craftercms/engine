package org.craftercms.engine.config;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.craftercms.engine.exception.ConfigurationException;

/**
 * Parses a configuration to create an object as result.
 *
 * @author avasquez
 */
public interface ConfigurationParser<T> {

    /**
     * Parses the specified config.
     *
     * @param config the config to parse
     * @return the object created from the config
     */
    T parse(HierarchicalConfiguration config) throws ConfigurationException;

}
