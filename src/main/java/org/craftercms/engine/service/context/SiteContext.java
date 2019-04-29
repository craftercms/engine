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
package org.craftercms.engine.service.context;

import graphql.GraphQL;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.exception.GraphQLBuildException;
import org.craftercms.engine.graphql.GraphQLFactory;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.PreviewOverlayCallback;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import java.net.URLClassLoader;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wrapper for a {@link Context} that adds properties specific to Crafter Engine.
 *
 * @author Alfonso Vásquez
 */
public class SiteContext {

    private static final Logger logger = LoggerFactory.getLogger(SiteContext.class);

    private static final String SITE_NAME_MDC_KEY = "siteName";

    private static ThreadLocal<SiteContext> threadLocal = new ThreadLocal<>();

    public static final String CACHE_CLEARED_EVENT_KEY = "cacheCleared";
    public static final String CONTEXT_BUILT_EVENT_KEY = "contextBuilt";
    public static final String GRAPHQL_BUILT_EVENT_KEY = "graphQLBuilt";

    protected ContentStoreService storeService;
    protected String siteName;
    protected Context context;
    protected boolean fallback;
    protected String staticAssetsPath;
    protected String templatesPath;
    protected String restScriptsPath;
    protected String controllerScriptsPath;
    protected FreeMarkerConfig freeMarkerConfig;
    protected UrlTransformationEngine urlTransformationEngine;
    protected PreviewOverlayCallback overlayCallback;
    protected ScriptFactory scriptFactory;
    protected HierarchicalConfiguration config;
    protected ApplicationContext globalApplicationContext;
    protected ConfigurableApplicationContext applicationContext;
    protected URLClassLoader classLoader;
    protected UrlRewriter urlRewriter;
    protected Scheduler scheduler;
    protected Map<String, Instant> events;
    protected Lock graphQLBuildLock;
    protected GraphQLFactory graphQLFactory;
    protected GraphQL graphQL;

    /**
     * Returns the context for the current thread.
     */
    public static SiteContext getCurrent() {
        return threadLocal.get();
    }

    /**
     * Sets the context for the current thread.
     */
    public static void setCurrent(SiteContext current) {
        threadLocal.set(current);

        MDC.put(SITE_NAME_MDC_KEY, current.getSiteName());
    }

    /**
     * Removes the context from the current thread.
     */
    public static void clear() {
        MDC.remove(SITE_NAME_MDC_KEY);

        threadLocal.remove();
    }

    public SiteContext() {
        graphQLBuildLock = new ReentrantLock();

        Instant now = Instant.now();

        events = new ConcurrentHashMap<>();
        events.put(CACHE_CLEARED_EVENT_KEY, now);
        events.put(CONTEXT_BUILT_EVENT_KEY, now);
        events.put(GRAPHQL_BUILT_EVENT_KEY, now);
    }

    public ContentStoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    public String getStaticAssetsPath() {
        return staticAssetsPath;
    }

    public void setStaticAssetsPath(String staticAssetsPath) {
        this.staticAssetsPath = staticAssetsPath;
    }

    public String getTemplatesPath() {
        return templatesPath;
    }

    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }

    public String getRestScriptsPath() {
        return restScriptsPath;
    }

    public void setRestScriptsPath(String restScriptsPath) {
        this.restScriptsPath = restScriptsPath;
    }

    public String getControllerScriptsPath() {
        return controllerScriptsPath;
    }

    public void setControllerScriptsPath(String controllerScriptsPath) {
        this.controllerScriptsPath = controllerScriptsPath;
    }

    public FreeMarkerConfig getFreeMarkerConfig() {
        return freeMarkerConfig;
    }

    public void setFreeMarkerConfig(FreeMarkerConfig freeMarkerConfig) {
        this.freeMarkerConfig = freeMarkerConfig;
    }

    public UrlTransformationEngine getUrlTransformationEngine() {
        return urlTransformationEngine;
    }

    public void setUrlTransformationEngine(UrlTransformationEngine urlTransformationEngine) {
        this.urlTransformationEngine = urlTransformationEngine;
    }

    public PreviewOverlayCallback getOverlayCallback() {
        return overlayCallback;
    }

    public void setOverlayCallback(PreviewOverlayCallback overlayCallback) {
        this.overlayCallback = overlayCallback;
    }

    public ScriptFactory getScriptFactory() {
        return scriptFactory;
    }

    public void setScriptFactory(ScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    public HierarchicalConfiguration getConfig() {
        return config;
    }

    public void setConfig(HierarchicalConfiguration config) {
        this.config = config;
    }

    public ApplicationContext getGlobalApplicationContext() {
        return globalApplicationContext;
    }

    public void setGlobalApplicationContext(ApplicationContext globalApplicationContext) {
        this.globalApplicationContext = globalApplicationContext;
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public UrlRewriter getUrlRewriter() {
        return urlRewriter;
    }

    public void setUrlRewriter(UrlRewriter urlRewriter) {
        this.urlRewriter = urlRewriter;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public GraphQLFactory getGraphQLFactory() {
        return graphQLFactory;
    }

    public void setGraphQLFactory(GraphQLFactory graphQLFactory) {
        this.graphQLFactory = graphQLFactory;
    }

    public Map<String, Instant> getEvents() {
        return events;
    }

    public GraphQL getGraphQL() {
        return graphQL;
    }

    public boolean isValid() throws CrafterException {
        return storeService.validate(context);
    }

    public void clearCache(CacheService cacheService) {
        // Clear content cache
        cacheService.clearScope(context);
        // Clear Freemarker cache
        freeMarkerConfig.getConfiguration().clearTemplateCache();

        events.put(CACHE_CLEARED_EVENT_KEY, Instant.now());
    }

    public void buildGraphQLSchema() throws GraphQLBuildException {
        if (graphQLBuildLock.tryLock()) {
            logger.info("Starting GraphQL schema build for site '{}'", siteName);
            try {
                GraphQL graphQL = graphQLFactory.getInstance(this);
                if (Objects.nonNull(graphQL)) {
                    this.graphQL = graphQL;

                    events.put(GRAPHQL_BUILT_EVENT_KEY, Instant.now());
                }
            } catch (Exception e) {
                throw new GraphQLBuildException("Error building the GraphQL schema for site '" + siteName + "'", e);
            } finally {
                graphQLBuildLock.unlock();
            }
            logger.info("GraphQL schema build completed for site '{}'", siteName);
        } else {
            logger.info("GraphQL schema is already being built for site '{}'", siteName);
        }
    }

    public void destroy() throws CrafterException {
        storeService.destroyContext(context);

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
        if (scheduler != null) {
            try {
                scheduler.shutdown();
            } catch (SchedulerException e) {
                throw new CrafterException("Unable to shutdown scheduler", e);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SiteContext siteContext = (SiteContext) o;

        if (!siteName.equals(siteContext.siteName)) {
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
               '}';
    }

}
