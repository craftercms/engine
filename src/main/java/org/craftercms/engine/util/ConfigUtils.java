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
package org.craftercms.engine.util;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.interpol.Lookup;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.core.io.Resource;

/**
 * Configuration related utility methods.
 *
 * @author avasquez
 */
public class ConfigUtils {

    private ConfigUtils() {
    }

    /**
     * Returns the configuration from the current site context.
     */
    public static HierarchicalConfiguration getCurrentConfig() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return siteContext.getConfig();
        } else {
            return null;
        }
    }

    public static XMLConfiguration readXmlConfiguration(Resource resource, char listDelimiter) throws ConfigurationException {
        return readXmlConfiguration(resource, listDelimiter, null);
    }

    public static XMLConfiguration readXmlConfiguration(Resource resource, char listDelimiter,
                                                        Map<String, Lookup> prefixLookups) throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);

        try {
            XMLBuilderParameters xmlParams = params
                .xml()
                .setURL(resource.getURL())
                .setListDelimiterHandler(new DefaultListDelimiterHandler(listDelimiter));

            if (MapUtils.isNotEmpty(prefixLookups)) {
                xmlParams = xmlParams.setPrefixLookups(prefixLookups);
            }

            builder.configure(xmlParams);
        } catch (IOException e) {
            throw new ConfigurationException("Unable to get URL of resource " + resource, e);
        }

        return builder.getConfiguration();
    }

}
