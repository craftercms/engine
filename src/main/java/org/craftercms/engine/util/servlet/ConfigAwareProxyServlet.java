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
package org.craftercms.engine.util.servlet;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.http.client.utils.URIUtils;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.engine.exception.HttpProxyException;
import org.craftercms.engine.service.context.SiteContext;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Extension of {@link ProxyServlet} that uses the current site configuration
 *
 * @author joseross
 * @since 3.1.6
 */
public class ConfigAwareProxyServlet extends ProxyServlet {

    private static final Logger logger = LoggerFactory.getLogger(ConfigAwareProxyServlet.class);

    public static final String CONFIG_KEY_SERVERS = "servers.server";

    public static final String CONFIG_KEY_PATTERNS = "patterns.pattern";

    public static final String CONFIG_KEY_ID = "id";

    public static final String CONFIG_KEY_URL = "url";

    protected boolean preview;

    @Override
    protected void initTarget() {
        // Do nothing ... the target url will be resolved for each request
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        logger.debug("Starting execution of proxy request for {}", requestUri);

        SiteContext siteContext = SiteContext.getCurrent();

        if (siteContext == null) {
            throw new IllegalStateException("Can't resolve site context for current request");
        }

        // get the site id from the queryString
        String siteName = siteContext.getSiteName();

        logger.debug("Resolved site {} for proxy request {}", siteName, requestUri);

        // get the target url from the site config
        String targetUrl = getTargetUrl(siteContext, requestUri);

        logger.debug("Resolved target url {} for proxy request {}", targetUrl, requestUri);

        if (request.getRequestURL().toString().contains(targetUrl)) {
            logger.debug("Resolved target url for request {} is local, will skip proxy", requestUri);
            throw new HttpProxyException("Local target url detected");
        }

        // set the new target url
        request.setAttribute(ATTR_TARGET_URI, targetUrl);

        // set the new target host
        request.setAttribute(ATTR_TARGET_HOST, URIUtils.extractHost(URI.create(targetUrl)));

        // execute the proxy request
        super.service(request, response);
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
