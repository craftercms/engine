/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.plugin.impl;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.text.StringSubstitutor;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.EncryptionAwareConfigurationReader;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.plugin.PluginService;
import org.craftercms.engine.service.context.SiteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Default implementation for {@link PluginService}
 *
 * @author joseross
 * @since 4.0.0
 */
public class PluginServiceImpl implements PluginService {

    private static final Logger logger = LoggerFactory.getLogger(PluginService.class);

    public static final String PLUGIN_ID_KEY = "pluginId";

    public static final String PLUGIN_CONFIG_KEY = "pluginConfig";

    public static final String PLUGIN_ID_PLACEHOLDER = "pluginId";

    protected Pattern pattern = Pattern.compile(".*plugins/(.+)");

    protected ContentStoreService contentStoreService;

    protected EncryptionAwareConfigurationReader configurationReader;

    protected String configurationPathPattern;

    @ConstructorProperties({"contentStoreService", "configurationReader", "configurationPathPattern"})
    public PluginServiceImpl(ContentStoreService contentStoreService,
                             EncryptionAwareConfigurationReader configurationReader,
                             String configurationPathPattern) {
        this.contentStoreService = contentStoreService;
        this.configurationReader = configurationReader;
        this.configurationPathPattern = configurationPathPattern;
    }

    @Override
    public HierarchicalConfiguration<?> getPluginConfig(String pluginId) {
        Context context = getCurrentContext();
        String pluginPath = pluginId.replaceAll("\\.", File.separator);
        return loadPluginConfiguration(context, getPluginConfigPath(pluginPath));
    }

    protected HierarchicalConfiguration<?> loadPluginConfiguration(Context context, String pluginPath) {
        try (InputStream is =
                     contentStoreService.getContent(context, pluginPath).getInputStream()) {
            return configurationReader.readXmlConfiguration(is);
        } catch (ConfigurationException | IOException e) {
            logger.error("Error loading plugin configuration", e);
            return new XMLConfiguration();
        }
    }

    public void addPluginVariables(String url, BiConsumer<String, Object> setter) {
        Context context = getCurrentContext();
        Matcher matcher = pattern.matcher(url);

        if (!matcher.matches()) {
            // The url doesn't belong to a plugin for sure
            return;
        }

        // Iterate over all possible ids
        String parentUrl = matcher.group(1);
        boolean pluginFound = false;
        while (!pluginFound && isNotEmpty(parentUrl)) {
            parentUrl = FilenameUtils.getPathNoEndSeparator(parentUrl);
            pluginFound = pluginConfigExists(parentUrl);
        }

        if (isEmpty(parentUrl)) {
            return;
        }

        String pluginId = getPluginId(parentUrl);
        Configuration pluginConfig = loadPluginConfiguration(context, getPluginConfigPath(parentUrl));

        setter.accept(PLUGIN_ID_KEY, pluginId);
        setter.accept(PLUGIN_CONFIG_KEY, pluginConfig);
    }

    protected boolean pluginConfigExists(String path) {
        Context context = getCurrentContext();
        return contentStoreService.exists(context, getPluginConfigPath(path));
    }

    protected String getPluginId(String pluginPath) {
        return RegExUtils.replaceAll(pluginPath, File.separator, "\\.");
    }

    protected String getPluginConfigPath(String pluginPath) {
        return StringSubstitutor.replace(configurationPathPattern, Map.of(PLUGIN_ID_PLACEHOLDER, pluginPath));
    }

    protected Context getCurrentContext() {
        return Optional.ofNullable(SiteContext.getCurrent())
                .orElseThrow(IllegalStateException::new)
                .getContext();
    }

}
