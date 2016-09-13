/*
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

import org.apache.commons.configuration.Configuration;
import org.craftercms.engine.util.ConfigUtils;

/**
 * Common site configuration properties.
 *
 * @author avasquez
 */
public class CommonProperties {

    public static final String INDEX_FILE_NAME_CONFIG_KEY = "indexFileName";

    public static final String DEFAULT_INDEX_FILE_NAME = "index.xml";

    public static final String getIndexFileName() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(INDEX_FILE_NAME_CONFIG_KEY, DEFAULT_INDEX_FILE_NAME);
        } else {
            return DEFAULT_INDEX_FILE_NAME;
        }
    }

}
