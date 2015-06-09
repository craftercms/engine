package org.craftercms.engine.config.impl;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationBuilder;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.Content;
import org.craftercms.engine.service.context.SiteContext;

/**
 * A {@link ConfigurationBuilder} that retrieves the XML configuration from the Crafter content store.
 *
 * @author avasquez
 */
public class ContentStoreXmlConfigurationBuilder implements ConfigurationBuilder {

    private static final Log logger = LogFactory.getLog(ContentStoreXmlConfigurationBuilder.class);

    protected SiteContext context;
    protected String configPath;

    public ContentStoreXmlConfigurationBuilder(SiteContext context, String configPath) {
        this.context = context;
        this.configPath = configPath;
    }

    @Override
    public Configuration getConfiguration() throws ConfigurationException {
        Content configContent = context.getStoreService().findContent(context.getContext(), configPath);
        if (configContent != null) {
            XMLConfiguration config = new XMLConfiguration();
            String siteName = context.getSiteName();

            try {
                config.load(configContent.getInputStream());
            } catch (Exception e) {
                throw new ConfigurationException("Unable to load config file at " + configPath + " for site '" +
                                                 siteName + "'", e);
            }

            logger.info("Configuration loaded at " + configPath + " for site '" + siteName + "'");

            return config;
        } else {
            return null;
        }
    }

}
