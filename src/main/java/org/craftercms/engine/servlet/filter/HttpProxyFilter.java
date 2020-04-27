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

import org.craftercms.engine.exception.proxy.HttpProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Implementation of {@link javax.servlet.Filter} that delegates requests to a proxy
 *
 * @author joseross
 * @since 3.1.7
 */
public class HttpProxyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyFilter.class);

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
                proxyController.handleRequest(request, response);
                return;
            } catch (HttpProxyException e) {
                logger.debug("Continue with local execution for request " + request.getRequestURI());
            } catch (Exception e) {
                throw new ServletException("Error executing proxy request for " + request.getRequestURI(), e);
            }
        } else {
            logger.debug("Proxy is not enabled");
        }

        // Not enabled or proxy url is local, continue with normal execution
        filterChain.doFilter(request, response);
    }

}
