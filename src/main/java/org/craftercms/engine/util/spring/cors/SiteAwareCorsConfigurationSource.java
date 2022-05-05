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
package org.craftercms.engine.util.spring.cors;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link CorsConfigurationSource} that uses the current site configuration
 *
 * @author joseross
 * @since 3.1.11
 */
public class SiteAwareCorsConfigurationSource implements CorsConfigurationSource {

    public static final String CACHE_KEY = "-cors-config";

    public static final String CONFIG_KEY = "cors";
    public static final String ENABLE_KEY = "enable";
    public static final String MAX_AGE_KEY = "accessControlMaxAge";
    public static final String ALLOW_ORIGIN_KEY = "accessControlAllowOrigin";
    public static final String ALLOW_METHODS_KEY = "accessControlAllowMethods";
    public static final String ALLOW_HEADERS_KEY = "accessControlAllowHeaders";
    public static final String ALLOW_CREDENTIALS_KEY = "accessControlAllowCredentials";

    public static final long MAX_AGE_DEFAULT = 86400L;
    public static final String ALLOW_ORIGIN_DEFAULT = "*";
    public static final String ALLOW_METHODS_DEFAULT = "GET, POST, OPTIONS";
    public static final String ALLOW_HEADERS_DEFAULT = "Content-Type";
    public static final boolean ALLOW_CREDENTIALS_DEFAULT = true;

    protected CacheTemplate cacheTemplate;

    @ConstructorProperties({"cacheTemplate"})
    public SiteAwareCorsConfigurationSource(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        SiteContext siteContext = SiteContext.getCurrent();
        return cacheTemplate.getObject(siteContext.getContext(), () -> {
            try {
                HierarchicalConfiguration<?> config = siteContext.getConfig();
                HierarchicalConfiguration<?> corsConfig = config.configurationAt(CONFIG_KEY);
                if (corsConfig != null) {
                    return getConfiguration(corsConfig);
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }, CACHE_KEY);
    }

    protected CorsConfiguration getConfiguration(HierarchicalConfiguration<?> corsConfig) {
        if (corsConfig.getBoolean(ENABLE_KEY, false)) {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(getOrigins(corsConfig));
            config.setAllowedMethods(getValues(corsConfig, ALLOW_METHODS_KEY, ALLOW_METHODS_DEFAULT));
            config.setAllowedHeaders(getValues(corsConfig, ALLOW_HEADERS_KEY, ALLOW_HEADERS_DEFAULT));
            config.setMaxAge(corsConfig.getLong(MAX_AGE_KEY, MAX_AGE_DEFAULT));
            config.setAllowCredentials(corsConfig.getBoolean(ALLOW_CREDENTIALS_KEY, ALLOW_CREDENTIALS_DEFAULT));
            return config;
        } else {
            return null;
        }
    }

    protected List<String> getValues(HierarchicalConfiguration<?> config, String key, String defaultValue) {
        return Arrays.stream(config.getString(key, defaultValue).split(","))
                .map(String::trim)
                .collect(toList());
    }

    //This is a special case because each pattern can contain additional commas, so we can't split on all of them
    //The value should look like this "http://localhost:[8000\,3000], http://domain.com"
    protected List<String> getOrigins(HierarchicalConfiguration<?> config) {
        // Apache Commons Config will automatically split only the commas that are not escaped
        return config.getList(String.class, ALLOW_ORIGIN_KEY, List.of(ALLOW_ORIGIN_DEFAULT)).stream()
                .map(String::trim)
                .collect(toList());
    }

}
