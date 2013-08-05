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
package org.craftercms.engine.scripting.impl;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.util.cache.CacheCallback;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.exception.ScriptRenderingException;
import org.craftercms.engine.scripting.ScriptView;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/**
 * {@link org.craftercms.engine.scripting.ScriptViewResolver} implementation for {@link FreeMarkerScriptView}s.
 *
 * @author Alfonso Vásquez
 */
public class FreeMarkerScriptViewResolver extends AbstractScriptViewResolver implements ServletContextAware {

    private static final Log logger = LogFactory.getLog(FreeMarkerScriptViewResolver.class);

    protected CacheTemplate cacheTemplate;
    protected ServletContext servletContext;

    public FreeMarkerScriptViewResolver() {
        super("ftl");
    }

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected ScriptView getView(final String url, final String mimeType, final Locale locale) throws ScriptRenderingException {
        final SiteContext siteContext = AbstractSiteContextResolvingFilter.getCurrentContext();

        return cacheTemplate.execute(siteContext.getContext(), CachingOptions.DEFAULT_CACHING_OPTIONS, new CacheCallback<ScriptView>() {

            @Override
            public ScriptView doCacheable() throws ScriptRenderingException {
                Configuration configuration = siteContext.getRestScriptsFreeMarkerConfig().getConfiguration();
                ObjectWrapper objectWrapper = configuration.getObjectWrapper();

                if (logger.isDebugEnabled()) {
                    logger.debug("Trying to retrieve template at " + getTemplateLocation());
                }

                try {
                    Template template = getTemplate(configuration, url, locale);

                    return new FreeMarkerScriptView(mimeType, template, objectWrapper, servletContext);
                } catch (FileNotFoundException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Template not found at " + getTemplateLocation(), e);
                    }

                    // Return null to continue looking for views
                    return null;
                } catch (IOException e) {
                    throw new ScriptRenderingException("Error while retrieving template from " + getTemplateLocation(), e);
                }
            }

            protected String getTemplateLocation() {
                return "[site=" + siteContext.getSiteName() + ", url=" + url + "]";
            }

        }, url, mimeType, locale, "crafter.script.view");
    }

    protected Template getTemplate(Configuration configuration, String url, Locale locale) throws IOException {
        return configuration.getTemplate(url, locale);
    }

}
