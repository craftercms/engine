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

package org.craftercms.engine.scripting.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.commons.lang.Callback;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Servlet filter that passes the request through a series of scripts that act as filters too.
 *
 * @author avasquez
 */
public class ScriptFilter implements Filter {

    public static final String FILTER_KEY = "filters.filter";
    public static final String SCRIPT_KEY = "script";
    public static final String INCLUDE_MAPPINGS_KEY = "mapping.include";
    public static final String EXCLUDE_MAPPINGS_KEY = "mapping.exclude";

    public static final String FILTER_MAPPINGS_CACHE_KEY = "filterMappings";

    private ServletContext servletContext;
    private CacheTemplate cacheTemplate;
    protected PathMatcher pathMatcher;

    public ScriptFilter() {
        pathMatcher = new AntPathMatcher();
    }

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
        List<FilterMapping> filterMappings = getFilterMappings();
        if (CollectionUtils.isNotEmpty(filterMappings)) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            String requestUri = HttpUtils.getRequestUriWithoutContextPath(httpRequest);
            List<Script> scripts = new ArrayList<>();

            for (FilterMapping mapping : filterMappings) {
                if (!excludeRequest(requestUri, mapping.excludes) && includeRequest(requestUri, mapping.includes)) {
                    scripts.add(mapping.script);
                }
            }

            if (CollectionUtils.isNotEmpty(scripts)) {
                chain = new ScriptFilterChainImpl(scripts.iterator(), chain, servletContext);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @SuppressWarnings("unchecked")
    protected List<FilterMapping> getFilterMappings() {
        final SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            Callback<List<FilterMapping>> callback = new Callback<List<FilterMapping>>() {

                @Override
                public List<FilterMapping> execute() {
                    HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
                    CachingAwareList<FilterMapping> mappings = new CachingAwareList<>();

                    if (config != null) {
                        List<HierarchicalConfiguration> filtersConfig = config.configurationsAt(FILTER_KEY);
                        if (CollectionUtils.isNotEmpty(filtersConfig)) {
                            for (HierarchicalConfiguration filterConfig : filtersConfig) {
                                String scriptUrl = filterConfig.getString(SCRIPT_KEY);
                                String[] includes = filterConfig.getStringArray(INCLUDE_MAPPINGS_KEY);
                                String[] excludes = filterConfig.getStringArray(EXCLUDE_MAPPINGS_KEY);

                                if (StringUtils.isNotEmpty(scriptUrl) && ArrayUtils.isNotEmpty(includes)) {
                                    ContentStoreService storeService = siteContext.getStoreService();
                                    ScriptFactory scriptFactory = siteContext.getScriptFactory();

                                    if (!storeService.exists(siteContext.getContext(), scriptUrl)) {
                                        throw new ConfigurationException("No filter script found at " + scriptUrl);
                                    }

                                    FilterMapping mapping = new FilterMapping();
                                    mapping.script = scriptFactory.getScript(scriptUrl);
                                    mapping.includes = includes;
                                    mapping.excludes = excludes;

                                    mappings.add(mapping);
                                }
                            }
                        }
                    }

                    return mappings;
                }

            };

            return cacheTemplate.getObject(siteContext.getContext(), callback, FILTER_MAPPINGS_CACHE_KEY);
        } else {
            return null;
        }
    }

    protected boolean excludeRequest(String requestUri, String[] excludes) {
        if (ArrayUtils.isNotEmpty(excludes)) {
            for (String uriPattern : excludes) {
                if (pathMatcher.match(uriPattern, requestUri)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean includeRequest(String requestUri, String[] includes) {
        if (ArrayUtils.isNotEmpty(includes)) {
            for (String uriPattern : includes) {
                if (pathMatcher.match(uriPattern, requestUri)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected static class FilterMapping {

        private Script script;
        private String[] includes;
        private String[] excludes;

    }

}
