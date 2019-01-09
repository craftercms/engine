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
package org.craftercms.engine.properties;

import org.apache.commons.lang3.StringUtils;

/**
 * Global properties of the Engine instance, in other words, properties that are common to all sites.
 *
 * @author avasquez
 */
public class SystemProperties {

    public static final String MODE_PREVIEW_PROPERTY_NAME = "crafter.modePreview";
    public static final String ENVIRONMENT_PROPERTY_NAME = "crafter.environment";

    public static boolean isModePreview() {
        String value = System.getProperty(MODE_PREVIEW_PROPERTY_NAME);
        if (StringUtils.isNotEmpty(value)) {
            return Boolean.valueOf(value);
        } else {
            return false;
        }
    }

    public static String getEnvironment() {
        String value = System.getProperty(ENVIRONMENT_PROPERTY_NAME);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        } else {
            return null;
        }
    }

}
