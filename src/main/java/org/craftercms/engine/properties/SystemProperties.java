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
package org.craftercms.engine.properties;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
        if (isNotEmpty(value)) {
            return Boolean.parseBoolean(value);
        }

        return false;
    }

    public static String getEnvironment() {
        String value = System.getProperty(ENVIRONMENT_PROPERTY_NAME);
        if (isNotEmpty(value)) {
            return value;
        } else {
            return null;
        }
    }

}
