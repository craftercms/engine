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
package org.craftercms.engine.plugin;

import org.apache.commons.configuration2.HierarchicalConfiguration;

import java.util.function.BiConsumer;

/**
 * Defines operations related to plugin execution
 *
 * @author joseross
 * @since 4.0.0
 */
public interface PluginService {

    /**
     * Loads the configuration for a given plugin
     * @param pluginId the id of the plugin
     * @return the configuration object
     */
    HierarchicalConfiguration<?> getPluginConfig(String pluginId);

    /**
     * Adds variables related to plugins if the given URL belongs to a plugin
     * @param url the URL to check
     * @param setter the setter for the variables
     */
    void addPluginVariables(String url, BiConsumer<String, Object> setter);

}
