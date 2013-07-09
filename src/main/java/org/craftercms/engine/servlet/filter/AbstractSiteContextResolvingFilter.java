/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.util.HttpServletUtils;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextFactory;
import org.craftercms.engine.service.context.SiteContextRegistry;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract {@link javax.servlet.Filter} that exposes a {@link org.craftercms.engine.service.context.SiteContext} from the {@link org.craftercms.engine.service.context.SiteContextRegistry} as a session attribute. The context to
 * expose is determined by the site name returned by {@link #getSiteNameFromRequest(org.springframework.web.context.request.ServletWebRequest)}.
 *
 * @author Alfonso Vásquez
 */
public abstract class AbstractSiteContextResolvingFilter implements Filter {

    private static final Log logger = LogFactory.getLog(AbstractSiteContextResolvingFilter.class);

    public static final String SITE_NAME_ATTRIBUTE = "siteName";
    public static final String SITE_CONTEXT_ATTRIBUTE = "siteContext";

    protected Lock createContextLock;

    protected SiteContextRegistry siteContextRegistry;
    protected SiteContextFactory siteContextFactory;
    protected SiteContextFactory fallbackSiteContextFactory;
    protected String fallbackSiteName;

    public static SiteContext getCurrentContext() {
        return (SiteContext) HttpServletUtils.getAttribute(SITE_CONTEXT_ATTRIBUTE, HttpServletUtils.SCOPE_REQUEST);
    }

    protected AbstractSiteContextResolvingFilter() {
        createContextLock = new ReentrantLock();
    }

    @Required
    public void setSiteContextRegistry(SiteContextRegistry siteContextRegistry) {
        this.siteContextRegistry = siteContextRegistry;
    }

    @Required
    public void setSiteContextFactory(SiteContextFactory siteContextFactory) {
        this.siteContextFactory = siteContextFactory;
    }

    @Required
    public void setFallbackSiteContextFactory(SiteContextFactory fallbackSiteContextFactory) {
        this.fallbackSiteContextFactory = fallbackSiteContextFactory;
    }

    @Required
    public void setFallbackSiteName(String fallbackSiteName) {
        this.fallbackSiteName = fallbackSiteName;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter(new ServletWebRequest((HttpServletRequest) request, (HttpServletResponse) response), chain);
    }

    public void doFilter(ServletWebRequest request, FilterChain chain) throws IOException, ServletException {
        String siteName = StringUtils.lowerCase(getSiteNameFromRequest(request));

        if (StringUtils.isNotEmpty(siteName)) {
            handleSiteNameResolved(siteName, request, chain);
        } else {
            handleNoSiteNameResolved(request, chain);
        }
    }

    @Override
    public void destroy() {
    }

    protected SiteContext createAndRegisterContext(String siteName, boolean fallback) {
        createContextLock.lock();
        try {
            // Check again if the site context hasn't been created already by another thread
            SiteContext context = siteContextRegistry.get(siteName);
            if (context == null) {
                if (fallback) {
                    logger.info("Creating fallback site context '" + fallbackSiteName + "'...");

                    context = fallbackSiteContextFactory.createContext(siteName, true);
                } else {
                    logger.info("No context for site name '" + siteName + "' found. Creating new one...");

                    context = siteContextFactory.createContext(siteName, false);
                }

                siteContextRegistry.register(context);
            }

            return context;
        } finally {
            createContextLock.unlock();
        }
    }

    protected void handleSiteNameResolved(String siteName, ServletWebRequest request, FilterChain chain) throws IOException,
            ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("Site name resolved for current request: '" + siteName + "'");
        }
        
        setCurrentSiteNameAttribute(request, siteName);

        SiteContext context = siteContextRegistry.get(siteName);
        if (context == null) {
            context = createAndRegisterContext(siteName, false);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Site context resolved for current request: " + context);
        }

        setCurrentContextAttribute(request, context);

        chain.doFilter(request.getRequest(), request.getResponse());
    }

    protected void handleNoSiteNameResolved(ServletWebRequest request, FilterChain chain) throws IOException, ServletException {
        logger.warn("Unable to resolve a site name for the request. Using fallback site");

        setCurrentSiteNameAttribute(request, fallbackSiteName);

        SiteContext context = siteContextRegistry.get(fallbackSiteName);
        if (context == null) {
            context = createAndRegisterContext(fallbackSiteName, true);
        }

        setCurrentContextAttribute(request, context);

        chain.doFilter(request.getRequest(), request.getResponse());
    }

    private void setCurrentSiteNameAttribute(RequestAttributes requestAttributes, String siteName) {
        requestAttributes.setAttribute(SITE_NAME_ATTRIBUTE, siteName, RequestAttributes.SCOPE_REQUEST);
    }

    private void setCurrentContextAttribute(RequestAttributes requestAttributes, SiteContext context) {
        requestAttributes.setAttribute(SITE_CONTEXT_ATTRIBUTE, context, RequestAttributes.SCOPE_REQUEST);
    }

    protected abstract String getSiteNameFromRequest(ServletWebRequest request);

}
