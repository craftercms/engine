package org.craftercms.engine.util.config.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationBuilder;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link ConfigurationBuilder} that creates the configuration from a set of specified configuration paths. Each
 * configuration from this path is then combined in a single configuration, where the latest configurations override
 * the first ones.
 *
 * @author avasquez
 */
public class MultiConfigurationBuilder implements ConfigurationBuilder {

    private static final Log logger = LogFactory.getLog(MultiConfigurationBuilder.class);

    protected String[] configPaths;
    protected ResourceLoader resourceLoader;

    public MultiConfigurationBuilder(String[] configPaths, ResourceLoader resourceLoader) {
        this.configPaths = configPaths;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Configuration getConfiguration() throws ConfigurationException {
        List<Configuration> configs = new ArrayList<>();

        // Last configurations should be loaded and added first so that they have greater priority.
        logger.info("Loading XML configurations in the order in which the properties will be resolved");

        for (int i = configPaths.length - 1; i >= 0; i--) {
            try {
                Resource resource = resourceLoader.getResource(configPaths[i]);
                if (resource.exists()) {
                    XMLConfiguration config = new XMLConfiguration();
                    config.load(resource.getInputStream());

                    logger.info("XML configuration loaded from " + resource);

                    configs.add(config);
                }
            } catch (Exception e) {
                throw new ConfigurationException("Unable to load configuration at " + configPaths[i], e);
            }
        }

        if (configs.size() > 1) {
            CombinedConfiguration combinedConfig = new CombinedConfiguration(new OverrideCombiner());

            for (Configuration config : configs) {
                combinedConfig.addConfiguration((AbstractConfiguration)config);
            }

            return combinedConfig;
        } else if (configs.size() == 1) {
            return configs.get(0);
        } else {
            return null;
        }
    }

}
