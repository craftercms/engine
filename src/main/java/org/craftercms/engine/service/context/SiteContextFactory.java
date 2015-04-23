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
import java.util.Map;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.filesystem.FileSystemContentStoreAdapter;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.exception.SiteContextCreationException;
import org.craftercms.engine.macro.MacroResolver;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.impl.GroovyScriptFactory;
import org.craftercms.engine.service.PreviewOverlayCallback;
import org.craftercms.engine.util.groovy.ContentStoreGroovyResourceLoader;
import org.craftercms.engine.util.groovy.ContentStoreResourceConnector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.xml.sax.InputSource;

/**
 * Factory for creating {@link SiteContext} with common properties. It also uses the {@link MacroResolver} to resolve
 * any macros specified in the {@code rootFolderPath} before creating the context (remember that macros can vary
 * between requests).
 *
 * @author Alfonso Vásquez
 */
public class SiteContextFactory implements ApplicationContextAware {

    private static final Log logger = LogFactory.getLog(SiteContextFactory.class);

    protected String storeType;
    protected String storeServerUrl;
    protected String username;
    protected String password;
    protected String rootFolderPath;
    protected String staticAssetsPath;
    protected String templatesPath;
    protected String restScriptsPath;
    protected String controllerScriptsPath;
    protected String configPath;
    protected String applicationContextPath;
    protected String groovyClassesPath;
    protected Map<String, Object> groovyGlobalVars;
    protected boolean cacheOn;
    protected int maxAllowedItemsInCache;
    protected boolean ignoreHiddenFiles;
    protected ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory;
    protected UrlTransformationEngine urlTransformationEngine;
    protected PreviewOverlayCallback overlayCallback;
    protected ContentStoreService storeService;
    protected MacroResolver macroResolver;
    protected ApplicationContext mainApplicationContext;

    public SiteContextFactory() {
        storeType = FileSystemContentStoreAdapter.STORE_TYPE;
        storeServerUrl = null;
        username = null;
        password = null;
        cacheOn = Context.DEFAULT_CACHE_ON;
        maxAllowedItemsInCache = Context.DEFAULT_MAX_ALLOWED_ITEMS_IN_CACHE;
        ignoreHiddenFiles = Context.DEFAULT_IGNORE_HIDDEN_FILES;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public void setStoreServerUrl(String storeServerUrl) {
        this.storeServerUrl = storeServerUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Required
    public void setRootFolderPath(String rootFolderPath) {
        this.rootFolderPath = rootFolderPath;
    }

    @Required
    public void setStaticAssetsPath(String staticAssetsPath) {
        this.staticAssetsPath = staticAssetsPath;
    }

    @Required
    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }

    @Required
    public void setRestScriptsPath(String restScriptsPath) {
        this.restScriptsPath = restScriptsPath;
    }

    @Required
    public void setControllerScriptsPath(final String controllerScriptsPath) {
        this.controllerScriptsPath = controllerScriptsPath;
    }

    @Required
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    @Required
    public void setApplicationContextPath(String applicationContextPath) {
        this.applicationContextPath = applicationContextPath;
    }

    @Required
    public void setGroovyClassesPath(String groovyClassesPath) {
        this.groovyClassesPath = groovyClassesPath;
    }

    @Required
    public void setGroovyGlobalVars(Map<String, Object> groovyGlobalVars) {
        this.groovyGlobalVars = groovyGlobalVars;
    }

    public void setCacheOn(boolean cacheOn) {
        this.cacheOn = cacheOn;
    }

    public void setMaxAllowedItemsInCache(int maxAllowedItemsInCache) {
        this.maxAllowedItemsInCache = maxAllowedItemsInCache;
    }

    public void setIgnoreHiddenFiles(boolean ignoreHiddenFiles) {
        this.ignoreHiddenFiles = ignoreHiddenFiles;
    }

    @Required
    public void setFreeMarkerConfigFactory(ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory) {
        this.freeMarkerConfigFactory = freeMarkerConfigFactory;
    }

    @Required
    public void setUrlTransformationEngine(UrlTransformationEngine urlTransformationEngine) {
        this.urlTransformationEngine = urlTransformationEngine;
    }

    public void setOverlayCallback(PreviewOverlayCallback overlayCallback) {
        this.overlayCallback = overlayCallback;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.mainApplicationContext = applicationContext;
    }

    public SiteContext createContext(String siteName, boolean fallback) {
        String resolvedRootFolderPath = macroResolver.resolveMacros(rootFolderPath);

        Context context = storeService.createContext(storeType, storeServerUrl, username, password,
                                                     resolvedRootFolderPath, cacheOn, maxAllowedItemsInCache,
                                                     ignoreHiddenFiles);
        try {
            SiteContext siteContext = new SiteContext();
            siteContext.setStoreService(storeService);
            siteContext.setSiteName(siteName);
            siteContext.setContext(context);
            siteContext.setFallback(fallback);
            siteContext.setStaticAssetsPath(staticAssetsPath);
            siteContext.setTemplatesPath(templatesPath);
            siteContext.setFreeMarkerConfig(getFreemarkerConfig());
            siteContext.setUrlTransformationEngine(urlTransformationEngine);
            siteContext.setOverlayCallback(overlayCallback);

            if (!fallback) {
                URLClassLoader classLoader = getClassLoader(siteContext);

                siteContext.setRestScriptsPath(restScriptsPath);
                siteContext.setControllerScriptsPath(controllerScriptsPath);
                siteContext.setConfigPath(configPath);
                siteContext.setApplicationContextPath(applicationContextPath);
                siteContext.setGroovyClassesPath(groovyClassesPath);
                siteContext.setScriptFactory(getScriptFactory(siteContext, classLoader));
                siteContext.setConfig(getConfig(siteContext));
                siteContext.setApplicationContext(getApplicationContext(siteContext, classLoader));
                siteContext.setClassLoader(classLoader);
            }

            return siteContext;
        } catch (RuntimeException e) {
            // Destroy context if the site context creation failed
            storeService.destroyContext(context);

            throw e;
        }
    }

    protected FreeMarkerConfig getFreemarkerConfig() {
        return freeMarkerConfigFactory.getObject();
    }

    protected XMLConfiguration getConfig(SiteContext siteContext) {
        Content configContent = storeService.findContent(siteContext.getContext(), configPath);
        if (configContent != null) {
            XMLConfiguration config = new XMLConfiguration();

            try {
                config.load(configContent.getInputStream());
            } catch (Exception e) {
                throw new SiteContextCreationException("Unable to load config file at " + configPath, e);
            }

            logger.info("Configuration loaded from " + configPath + " for site context '" + siteContext.getSiteName() +
                        "'");

            return config;
        } else {
            return null;
        }
    }

    protected URLClassLoader getClassLoader(SiteContext siteContext) {
        ContentStoreGroovyResourceLoader resourceLoader = new ContentStoreGroovyResourceLoader(siteContext,
                                                                                               groovyClassesPath);
        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().getClassLoader());

        classLoader.setResourceLoader(resourceLoader);

        return classLoader;
    }

    protected ConfigurableApplicationContext getApplicationContext(SiteContext siteContext,
                                                                   URLClassLoader classLoader) {
        Content appContextContent = storeService.findContent(siteContext.getContext(), applicationContextPath);
        if (appContextContent != null) {
            GenericApplicationContext appContext = new GenericApplicationContext(mainApplicationContext);
            appContext.setClassLoader(classLoader);

            try {
                XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
                xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
                xmlReader.loadBeanDefinitions(new InputSource(appContextContent.getInputStream()));

                appContext.refresh();
            } catch (Exception e) {
                throw new SiteContextCreationException("Unable to load application context at " +
                                                       applicationContextPath, e);
            }

            logger.info("Application context loaded from " + applicationContextPath + " for site context '" +
                        siteContext.getSiteName() + "'");

            return appContext;
        } else {
            return null;
        }
    }

    protected ScriptFactory getScriptFactory(SiteContext siteContext, URLClassLoader classLoader) {
        ContentStoreResourceConnector resourceConnector = new ContentStoreResourceConnector(siteContext);

        return new GroovyScriptFactory(resourceConnector, classLoader, groovyGlobalVars);
    }

}
