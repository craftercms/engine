/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.servlet.filter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.commons.lang.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet filter that add headers to response if there are matched configuration patterns in site-config.xml
 */
public class HttpHeadersRewriteFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpHeadersRewriteFilter.class);
    private static final String CONFIG_KEY_MAPPINGS = "headerMappings.mapping";
    private static final String CONFIG_KEY_PATTERNS = "urlPattern";
    private static final String CONFIG_KEY_HEADERS = "headers.header";
    private static final String CONFIG_KEY_HEADER_NAME = "name";
    private static final String CONFIG_KEY_HEADER_VALUE = "value";
    private static final String HEADER_MAPPINGS_CACHE_KEY = "headerMappings";

    private CacheTemplate cacheTemplate;
    private PathMatcher pathMatcher;

    @ConstructorProperties({"cacheTemplate"})
    public HttpHeadersRewriteFilter(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestUri = HttpUtils.getRequestUriWithoutContextPath(request);
            logger.debug("Try executing HTTP headers rewrite on response for request {}", requestUri);
            List<HeaderMapping> headerMappings = getHeaderMappings(requestUri);
            if (CollectionUtils.isNotEmpty(headerMappings)) {
                for (HeaderMapping headerMapping: headerMappings) {
                    String urlPattern = headerMapping.urlPattern;
                    Map<String, String> headers = headerMapping.headers;
                    logger.debug("Apply header mapping for request '{}' with pattern '{}'", requestUri, urlPattern);
                    for (Map.Entry<String, String> header: headers.entrySet()) {
                        String name = header.getKey();
                        String value = header.getValue();
                        logger.debug("Add header with name = '{}', value = '{}' to the response of request '{}'",
                                name, value, requestUri);
                        response.setHeader(name, value);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while executing HTTP headers rewrite for request '{}", request.getRequestURI(), e);
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<HeaderMapping> getHeaderMappings(String requestUri) {
        final SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            return null;
        }

        Callback<List<HeaderMapping>> callback = new Callback<>() {

            @Override
            public List<HeaderMapping> execute() {
                HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
                CachingAwareList<HeaderMapping> mappings = new CachingAwareList<>();
                if (config == null) {
                    return mappings;
                }

                List<HierarchicalConfiguration> mappingsConfig = config.configurationsAt(CONFIG_KEY_MAPPINGS);
                if (CollectionUtils.isEmpty(mappingsConfig)) {
                    return mappings;
                }

                for (HierarchicalConfiguration mappingConfig : mappingsConfig) {
                    String urlPattern = mappingConfig.getString(CONFIG_KEY_PATTERNS);
                    if (pathMatcher.match(urlPattern, requestUri)) {
                        logger.debug("Found matching url pattern '{}' for request '{}'", urlPattern, requestUri);
                        List<HierarchicalConfiguration> headersConfig = mappingConfig.configurationsAt(CONFIG_KEY_HEADERS);
                        if (CollectionUtils.isNotEmpty(headersConfig)) {
                            Map<String, String> headers = new HashMap<>();
                            for (HierarchicalConfiguration headerConfig : headersConfig) {
                                String name = headerConfig.getString(CONFIG_KEY_HEADER_NAME);
                                String value = headerConfig.getString(CONFIG_KEY_HEADER_VALUE);
                                headers.put(name, value);
                            }

                            if (!headers.isEmpty()) {
                                HeaderMapping mapping = new HeaderMapping();
                                mapping.urlPattern = urlPattern;
                                mapping.headers = headers;
                                mappings.add(mapping);
                            }
                        }
                    }
                }

                return mappings;
            }
        };

        return cacheTemplate.getObject(siteContext.getContext(), callback, HEADER_MAPPINGS_CACHE_KEY);
    }

    protected static class HeaderMapping {
        String urlPattern;
        Map<String, String> headers;
    }
}