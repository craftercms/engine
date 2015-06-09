package org.craftercms.engine.config.impl;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationBuilder;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * A {@link ConfigurationBuilder} that retrieves the XML configuration from the classpath.
 *
 * @author avasquez
 */
public class ClasspathXmlConfigurationBuilder implements ConfigurationBuilder {

    private static final Log logger = LogFactory.getLog(ClasspathXmlConfigurationBuilder.class);

    protected String configPath;

    public ClasspathXmlConfigurationBuilder(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public Configuration getConfiguration() throws ConfigurationException {
        Resource configResource = new ClassPathResource(configPath);
        if (configResource.exists()) {
            XMLConfiguration config = new XMLConfiguration();

            try {
                config.load(configResource.getInputStream());
            } catch (Exception e) {
                throw new ConfigurationException("Unable to load config file at classpath " + configPath, e);
            }

            logger.info("Configuration loaded at classpath " + configPath);

            return config;
        } else {
            return null;
        }
    }

}
