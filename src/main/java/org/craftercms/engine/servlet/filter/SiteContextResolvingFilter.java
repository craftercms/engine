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

import org.craftercms.core.exception.CrafterException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Filter that uses a {@link org.craftercms.engine.service.context.SiteContextResolver} to resolve the context for
 * the current request. The site context and the site name are then set as request attributes.
 *
 * @author avasquez
 */
public class SiteContextResolvingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SiteContextResolvingFilter.class);

    protected SiteContextResolver contextResolver;

    @Required
    public void setContextResolver(SiteContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        SiteContext.setCurrent(getContext((HttpServletRequest) request));
        try {
            chain.doFilter(request, response);
        } finally {
            SiteContext.clear();
        }
    }

    protected SiteContext getContext(HttpServletRequest request) throws ServletException {
        try {
            SiteContext siteContext = contextResolver.getContext(request);
            if (siteContext != null) {
                return siteContext;
            } else {
                throw new CrafterException("No site context was resolved for the current request");
            }
        } catch (Exception e) {
            logger.error("Error while resolving site context for current request", e);

            throw new ServletException("Error while resolving site context for current request", e);
        }
    }

    @Override
    public void destroy() {

    }

}
