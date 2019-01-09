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

package org.craftercms.engine.util.config;

import org.apache.commons.configuration2.HierarchicalConfiguration;
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
