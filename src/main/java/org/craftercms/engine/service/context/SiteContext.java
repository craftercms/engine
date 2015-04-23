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
package org.craftercms.engine.service.context;

import java.net.URLClassLoader;

import org.apache.commons.configuration.XMLConfiguration;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.PreviewOverlayCallback;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

/**
 * Wrapper for a {@link Context} that adds properties specific to Crafter Engine.
 *
 * @author Alfonso Vásquez
 */
public class SiteContext {

    public static final String SITE_CONTEXT_ATTRIBUTE = "siteContext";

    protected ContentStoreService storeService;
    protected String siteName;
    protected Context context;
    protected boolean fallback;
    protected String staticAssetsPath;
    protected String templatesPath;
    protected String restScriptsPath;
    protected String controllerScriptsPath;
    protected String configPath;
    protected String applicationContextPath;
    protected String groovyClassesPath;
    protected FreeMarkerConfig freeMarkerConfig;
    protected UrlTransformationEngine urlTransformationEngine;
    protected PreviewOverlayCallback overlayCallback;
    protected ScriptFactory scriptFactory;
    protected XMLConfiguration config;
    protected ConfigurableApplicationContext applicationContext;
    protected URLClassLoader classLoader;

    public static SiteContext getCurrent() {
        RequestContext requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            return (SiteContext)requestContext.getRequest().getAttribute(SITE_CONTEXT_ATTRIBUTE);
        } else {
            return null;
        }
    }

    public static void setCurrent(SiteContext context) {
        RequestContext requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            requestContext.getRequest().setAttribute(SITE_CONTEXT_ATTRIBUTE, context);
        }
    }

    public ContentStoreService getStoreService() {
        return storeService;
    }

    public String getSiteName() {
        return siteName;
    }

    public Context getContext() {
        return context;
    }

    public boolean isFallback() {
        return fallback;
    }

    public String getStaticAssetsPath() {
        return staticAssetsPath;
    }

    public String getTemplatesPath() {
        return templatesPath;
    }

    public String getRestScriptsPath() {
        return restScriptsPath;
    }

    public String getControllerScriptsPath() {
        return controllerScriptsPath;
    }

    public String getConfigPath() {
        return configPath;
    }

    public String getApplicationContextPath() {
        return applicationContextPath;
    }

    public String getGroovyClassesPath() {
        return groovyClassesPath;
    }

    public FreeMarkerConfig getFreeMarkerConfig() {
        return freeMarkerConfig;
    }

    public UrlTransformationEngine getUrlTransformationEngine() {
        return urlTransformationEngine;
    }

    public PreviewOverlayCallback getOverlayCallback() {
        return overlayCallback;
    }

    public ScriptFactory getScriptFactory() {
        return scriptFactory;
    }

    public XMLConfiguration getConfig() {
        return config;
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    @Required
    public void setContext(Context context) {
        this.context = context;
    }

    @Required
    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    @Required
    public void setStaticAssetsPath(String staticAssetsPath) {
        this.staticAssetsPath = staticAssetsPath;
    }

    @Required
    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }

    public void setRestScriptsPath(String restScriptsPath) {
        this.restScriptsPath = restScriptsPath;
    }

    public void setControllerScriptsPath(String controllerScriptsPath) {
        this.controllerScriptsPath = controllerScriptsPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public void setApplicationContextPath(String applicationContextPath) {
        this.applicationContextPath = applicationContextPath;
    }

    public void setGroovyClassesPath(String groovyClassesPath) {
        this.groovyClassesPath = groovyClassesPath;
    }

    @Required
    public void setFreeMarkerConfig(FreeMarkerConfig freeMarkerConfig) {
        this.freeMarkerConfig = freeMarkerConfig;
    }

    @Required
    public void setUrlTransformationEngine(UrlTransformationEngine urlTransformationEngine) {
        this.urlTransformationEngine = urlTransformationEngine;
    }

    public void setOverlayCallback(PreviewOverlayCallback overlayCallback) {
        this.overlayCallback = overlayCallback;
    }

    public void setScriptFactory(ScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    public void setConfig(XMLConfiguration config) {
        this.config = config;
    }

    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void destroy() throws CrafterException {
        if (applicationContext != null) {
            try {
                applicationContext.close();
            } catch (Exception e) {
                throw new CrafterException("Unable to close application context", e);
            }
        }
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (Exception e) {
                throw new CrafterException("Unable to close class loader", e);
            }
        }

        storeService.destroyContext(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SiteContext context = (SiteContext) o;

        if (!siteName.equals(context.siteName)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return siteName.hashCode();
    }

    @Override
    public String toString() {
        return "SiteContext{" +
               "siteName='" + siteName + '\'' +
               ", context=" + context +
               ", fallback=" + fallback +
               ", staticAssetsPath='" + staticAssetsPath + '\'' +
               ", templatesPath='" + templatesPath + '\'' +
               ", restScriptsPath='" + restScriptsPath + '\'' +
               ", controllerScriptsPath='" + controllerScriptsPath + '\'' +
               ", configPath='" + configPath + '\'' +
               ", applicationContextPath='" + applicationContextPath + '\'' +
               ", groovyClassesPath='" + groovyClassesPath + '\'' +
               '}';
    }

}
