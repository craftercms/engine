/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.engine.util.config.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilder;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.config.DecryptionLookup;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link ConfigurationBuilder} that creates the configuration from a set of specified resource configuration paths. Each
 * configuration from this path is then combined in a single configuration, where the latest configurations override the
 * first ones.
 *
 * @author avasquez
 */
public class MultiResourceConfigurationBuilder implements ConfigurationBuilder<HierarchicalConfiguration> {

    private static final Log logger = LogFactory.getLog(MultiResourceConfigurationBuilder.class);

    public static final char DEFAULT_LIST_DELIMITER = ',';
    public static final String DEFAULT_ENCRYPTED_VALUE_PREFIX = "enc";

    protected String[] configPaths;
    protected ResourceLoader resourceLoader;
    protected char configListDelimiter;
    protected String encryptedValuePrefix;
    protected TextEncryptor configDecryptor;

    public MultiResourceConfigurationBuilder(String[] configPaths, ResourceLoader resourceLoader) {
        this(configPaths, resourceLoader, DEFAULT_LIST_DELIMITER, DEFAULT_ENCRYPTED_VALUE_PREFIX, null);
    }

    public MultiResourceConfigurationBuilder(String[] configPaths, ResourceLoader resourceLoader, TextEncryptor configDecryptor) {
        this(configPaths, resourceLoader, DEFAULT_LIST_DELIMITER, DEFAULT_ENCRYPTED_VALUE_PREFIX, configDecryptor);
    }

    public MultiResourceConfigurationBuilder(String[] configPaths, ResourceLoader resourceLoader, char configListDelimiter,
                                             String encryptedValuePrefix, TextEncryptor configDecryptor) {
        this.configPaths = configPaths;
        this.resourceLoader = resourceLoader;
        this.configListDelimiter = configListDelimiter;
        this.encryptedValuePrefix = encryptedValuePrefix;
        this.configDecryptor = configDecryptor;
    }

    @Override
    public HierarchicalConfiguration getConfiguration() throws ConfigurationException {
        List<HierarchicalConfiguration> configs = new ArrayList<>();

        // Last configurations should be loaded and added first so that they have greater priority.
        logger.info("Loading XML configurations in the order in which the properties will be resolved");

        for (int i = configPaths.length - 1; i >= 0; i--) {
            try {
                Resource resource = resourceLoader.getResource(configPaths[i]);
                if (resource.exists()) {
                    Map<String, Lookup> prefixLookups = null;
                    if (configDecryptor != null) {
                        prefixLookups = Collections.singletonMap(encryptedValuePrefix, new DecryptionLookup(configDecryptor));
                    }

                    XMLConfiguration config = ConfigUtils.readXmlConfiguration(resource, configListDelimiter, prefixLookups);

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
                combinedConfig.addConfiguration(config);
            }

            return combinedConfig;
        } else if (configs.size() == 1) {
            return configs.get(0);
        } else {
            return null;
        }
    }

    @Override
    public <E extends Event> void addEventListener(EventType<E> eventType, EventListener<? super E> listener) {
        // Not used
    }

    @Override
    public <E extends Event> boolean removeEventListener(EventType<E> eventType, EventListener<? super E> listener) {
        // Not used
        return false;
    }
}
