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

package org.craftercms.engine.servlet.filter;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.web.CORSFilter;
import org.craftercms.engine.service.context.SiteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter to include CORS headers based on the current site configuration.
 *
 * @author Jose Ross
 */
public class SiteAwareCORSFilter extends CORSFilter {

    private static final Logger logger = LoggerFactory.getLogger(SiteAwareCORSFilter.class);

    public static final String CONFIG_KEY = "cors";
    public static final String ENABLE_KEY = "enable";
    public static final String MAX_AGE_KEY = "accessControlMaxAge";
    public static final String ALLOW_ORIGIN_KEY = "accessControlAllowOrigin";
    public static final String ALLOW_METHODS_KEY = "accessControlAllowMethods";
    public static final String ALLOW_HEADERS_KEY = "accessControlAllowHeaders";
    public static final String ALLOW_CREDENTIALS_KEY = "accessControlAllowCredentials";

    public static final String MAX_AGE_DEFAULT = "86400";
    public static final String ALLOW_ORIGIN_DEFAULT = "*";
    public static final String ALLOW_METHODS_DEFAULT = "GET, POST, OPTIONS";
    public static final String ALLOW_HEADERS_DEFAULT = "Content-Type";
    public static final String ALLOW_CREDENTIALS_DEFAULT = "true";

    @Override
    public boolean isDisableCORS() {
        SiteContext siteContext = SiteContext.getCurrent();
        HierarchicalConfiguration config = siteContext.getConfig();
        try {
            HierarchicalConfiguration corsConfig = config.configurationAt(CONFIG_KEY);
            if (corsConfig != null) {
                return !corsConfig.getBoolean(ENABLE_KEY, false);
            }
        } catch (Exception e) {
            logger.debug("Site '{}' has no CORS configuration", siteContext.getSiteName());
        }
        return true;
    }

    protected String getValue(String key, String defaultValue) {
        SiteContext siteContext = SiteContext.getCurrent();
        HierarchicalConfiguration corsConfig = siteContext.getConfig().configurationAt(CONFIG_KEY);
        return corsConfig.getString(key, defaultValue);
    }

    @Override
    public String getAllowOrigins() {
        return getValue(ALLOW_ORIGIN_KEY, ALLOW_ORIGIN_DEFAULT);
    }

    @Override
    public String getAllowMethods() {
        return getValue(ALLOW_METHODS_KEY, ALLOW_METHODS_DEFAULT);
    }

    @Override
    public String getMaxAge() {
        return getValue(MAX_AGE_KEY, MAX_AGE_DEFAULT);
    }

    @Override
    public String getAllowHeaders() {
        return getValue(ALLOW_HEADERS_KEY, ALLOW_HEADERS_DEFAULT);
    }

    @Override
    public String getAllowCredentials() {
        return getValue(ALLOW_CREDENTIALS_KEY, ALLOW_CREDENTIALS_DEFAULT);
    }
}
