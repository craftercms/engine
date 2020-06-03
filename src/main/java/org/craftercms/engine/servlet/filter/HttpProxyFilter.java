/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;

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

    public HttpProxyFilter(boolean enabled, Controller proxyController) {
        this.enabled = enabled;
        this.proxyController = proxyController;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (enabled) {
            logger.debug("Trying to execute proxy request for {}", request.getRequestURI());
            try {
                String requestUri = request.getRequestURI();

                // get the target url from the site config
                String targetUrl = getTargetUrl(SiteContext.getCurrent(), request.getRequestURI());

                if (request.getRequestURL().toString().contains(targetUrl)) {
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
        } else {
            logger.debug("Proxy is not enabled");
        }

        // Not enabled or proxy url is local, continue with normal execution
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
                logger.debug("Found matching server {} for proxy request {}",
                        server.getString(CONFIG_KEY_ID), requestUri);
                return server.getString(CONFIG_KEY_URL);
            }
        }

        // should never happen (unless there is an issue with the config)
        throw new IllegalStateException("Invalid proxy configuration for site " + siteContext.getSiteName() +
                ", no matching server found for request " + requestUri);
    }

}
