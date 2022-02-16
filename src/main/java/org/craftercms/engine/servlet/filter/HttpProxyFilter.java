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
package org.craftercms.engine.servlet.filter;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.http.client.utils.URIUtils;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.engine.exception.proxy.HttpProxyException;
import org.craftercms.engine.service.context.SiteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.craftercms.engine.util.servlet.ConfigAwareProxyServlet.ATTR_TARGET_HOST;
import static org.craftercms.engine.util.servlet.ConfigAwareProxyServlet.ATTR_TARGET_URI;

/**
 * Implementation of {@link javax.servlet.Filter} that delegates requests to a proxy
 *
 * @author joseross
 * @since 3.1.7
 */
public class HttpProxyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyFilter.class);

    public static final String CONFIG_KEY_SERVERS = "servers.server";

    public static final String CONFIG_KEY_PATTERNS = "patterns.pattern";

    public static final String CONFIG_KEY_ID = "id";

    public static final String CONFIG_KEY_URL = "url";

    /**
     * Indicates if the proxy is enabled
     */
    protected boolean enabled;

    /**
     * The proxy to use
     */
    protected Controller proxyController;

    protected RequestMatcher excludedMatcher;

    @ConstructorProperties({"enabled", "proxyController", "excludedUrls"})
    public HttpProxyFilter(boolean enabled, Controller proxyController, String[] excludedUrls) {
        this.enabled = enabled;
        this.proxyController = proxyController;
        excludedMatcher = new OrRequestMatcher(Stream.of(excludedUrls)
                .map(AntPathRequestMatcher::new)
                .collect(toList()));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || excludedMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            logger.debug("Trying to execute proxy request for {}", request.getRequestURI());
        try {
            String requestUri = request.getRequestURI();

            // get the target url from the site config
            String targetUrl = getTargetUrl(SiteContext.getCurrent(), request.getRequestURI());

            if (isEmpty(targetUrl) || request.getRequestURL().toString().contains(targetUrl)) {
                logger.debug("Resolved target url for request {} is local, will skip proxy", requestUri);
            } else {
                logger.debug("Resolved target url {} for proxy request {}", targetUrl, requestUri);
                // set the new target url
                request.setAttribute(ATTR_TARGET_URI, targetUrl);

                // set the new target host
                request.setAttribute(ATTR_TARGET_HOST, URIUtils.extractHost(URI.create(targetUrl)));

                // execute the proxy request
                logger.debug("Starting execution of proxy request for {}", requestUri);
                proxyController.handleRequest(request, response);
                return;
            }
        } catch (HttpProxyException e) {
            logger.debug("Continue with local execution for request " + request.getRequestURI(), e);
        } catch (Exception e) {
            throw new ServletException("Error executing proxy request for " + request.getRequestURI(), e);
        }

        // Proxy url is local, continue with normal execution
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("rawtypes, unchecked")
    protected String getTargetUrl(SiteContext siteContext, String requestUri) {
        HierarchicalConfiguration proxyConfig = siteContext.getProxyConfig();

        if (proxyConfig == null) {
            throw new HttpProxyException("No proxy configuration found for site " + siteContext.getSiteName());
        }

        List<HierarchicalConfiguration> servers = proxyConfig.configurationsAt(CONFIG_KEY_SERVERS);
        for (HierarchicalConfiguration server : servers) {
            List<String> patterns = server.getList(String.class, CONFIG_KEY_PATTERNS);
            if (RegexUtils.matchesAny(requestUri, patterns)) {
                logger.debug("Found matching server '{}' for proxy request {}",
                        server.getString(CONFIG_KEY_ID), requestUri);
                return server.getString(CONFIG_KEY_URL, null);
            }
        }

        return null;
    }

}
