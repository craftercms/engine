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
package org.craftercms.engine.url.rewrite;

import org.craftercms.engine.http.impl.DefaultExceptionHandler;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.tuckey.web.filters.urlrewrite.UrlRewriteWrappedResponse;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Crafter's implementation of Tuckey's {@code org.tuckey.web.filters.urlrewrite.UrlRewriteFilter}. It uses the same
 * {@code org.tuckey.web.filters.urlrewrite.UrlRewriter}, but skips most of the Servlet filter configuration and
 * uses per-site configuration, which can be specified in {@code /config/engine/urlrewrite.xml} (for Tuckey's classic
 * XML style configuration) or {@code /config/engine/urlrewrite.conf} (for Apache's mod_rewrite style configuration).
 *
 * @author avasquez
 *
 * @see <a href="http://tuckey.org/urlrewrite/">Tuckey URL Rewrite</a>
 */
public class UrlRewriteFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UrlRewriteFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // Do nothing
    }

    @PostConstruct
    public void init() {
        // Set Tuckey logging to use slf4j
        Log.setLevel("slf4j");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        UrlRewriter urlRewriter = getUrlRewriter();
        boolean requestRewritten = false;

        if (urlRewriter != null) {
            httpServletResponse = new UrlRewriteWrappedResponse(httpServletResponse, httpServletRequest, urlRewriter);
            requestRewritten = urlRewriter.processRequest(httpServletRequest, httpServletResponse, chain);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("URL rewriter engine not loaded, ignoring request");
            }
        }

        // if no rewrite has taken place continue as normal
        if (!requestRewritten) {
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    protected UrlRewriter getUrlRewriter() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return siteContext.getUrlRewriter();
        } else {
            throw new IllegalStateException("No site context found to get the URL rewriter from");
        }
    }

    @Override
    public void destroy() {
        // Do nothing
    }

}
