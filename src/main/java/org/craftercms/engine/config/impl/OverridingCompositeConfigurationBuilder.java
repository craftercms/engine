package org.craftercms.engine.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationBuilder;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.tree.OverrideCombiner;

/**
 * {@link ConfigurationBuilder} that uses several {@link ConfigurationBuilder}s to retrieve configurations. If
 * there's more than one configuration, they're combined so that later configurations override former ones.
 *
 * @author avasquez
 */
public class OverridingCompositeConfigurationBuilder implements ConfigurationBuilder {

    private Collection<ConfigurationBuilder> builders;

    public OverridingCompositeConfigurationBuilder(Collection<ConfigurationBuilder> builders) {
        this.builders = builders;
    }

    @Override
    public Configuration getConfiguration() throws ConfigurationException {
        List<Configuration> configs = new ArrayList<>();

        for (ConfigurationBuilder builder : builders) {
            Configuration config = builder.getConfiguration();
            if (config != null) {
                configs.add(config);
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
