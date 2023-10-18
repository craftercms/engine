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

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import org.craftercms.commons.validation.ValidationRuntimeException;
import org.craftercms.core.exception.RootFolderNotFoundException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

/**
 * Filter that uses a {@link org.craftercms.engine.service.context.SiteContextResolver} to resolve the context for
 * the current request. The site context and the site name are then set as request attributes.
 *
 * @author avasquez
 */
public class SiteContextResolvingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SiteContextResolvingFilter.class);

    protected String errorTemplate;

    protected SiteContextResolver contextResolver;

    protected ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory;

    @Required
    public void setErrorTemplate(final String errorTemplate) {
        this.errorTemplate = errorTemplate;
    }

    @Required
    public void setContextResolver(SiteContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    @Required
    public void setFreeMarkerConfigFactory(final ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory) {
        this.freeMarkerConfigFactory = freeMarkerConfigFactory;
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        SiteContext siteContext = getContext((HttpServletRequest) request, (HttpServletResponse) response);
        if (siteContext == null) {
            return;
        }
        SiteContext.setCurrent(siteContext);
        try {
            chain.doFilter(request, response);
        } finally {
            SiteContext.clear();
        }
    }

    protected SiteContext getContext(HttpServletRequest request, HttpServletResponse response) {
        try {
            return contextResolver.getContext(request);
        } catch (RootFolderNotFoundException e) {
            renderError(response, HttpServletResponse.SC_NOT_FOUND, e);
        } catch (ValidationRuntimeException e) {
            renderError(response, HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (Exception e) {
            renderError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
        return null;
    }

    protected void renderError(HttpServletResponse response, int statusCode, Exception ex) {
        logger.error("Error while resolving site context for current request", ex);
        response.setStatus(statusCode);
        try {
            Configuration configuration = freeMarkerConfigFactory.getObject().getConfiguration();
            Template template = configuration.getTemplate(errorTemplate);
            SimpleHash model = new SimpleHash(configuration.getObjectWrapper());
            configuration.setAllSharedVariables(model);
            template.process(model, response.getWriter());
        } catch (Exception e) {
            logger.error("Error rendering template for site resolving error", e);
        }
    }

    @Override
    public void destroy() {

    }

}
