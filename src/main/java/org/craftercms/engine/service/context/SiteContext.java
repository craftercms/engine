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
import org.apache.commons.lang3.time.StopWatch;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.event.*;
import org.craftercms.engine.exception.GraphQLBuildException;
import org.craftercms.engine.exception.SiteContextInitializationException;
import org.craftercms.engine.graphql.GraphQLFactory;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.util.GroovyScriptUtils;
import org.craftercms.engine.cache.SiteCacheWarmer;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import javax.servlet.ServletContext;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Wrapper for a {@link Context} that adds properties specific to Crafter Engine.
 *
 * @author Alfonso Vásquez
 */
public class SiteContext {

    private static final Logger logger = LoggerFactory.getLogger(SiteContext.class);

    private static final String SITE_NAME_MDC_KEY = "siteName";

    private static ThreadLocal<SiteContext> threadLocal = new ThreadLocal<>();

    public enum State {
        CREATED,
        INITIALIZED,
        DESTROYED
    }

    protected ContentStoreService storeService;
    protected CacheService cacheService;
    protected String siteName;
    protected Context context;
    protected boolean fallback;
    protected String staticAssetsPath;
    protected String templatesPath;
    protected String restScriptsPath;
    protected String controllerScriptsPath;
    protected String initScriptPath;
    protected FreeMarkerConfig freeMarkerConfig;
    protected UrlTransformationEngine urlTransformationEngine;
    protected ScriptFactory scriptFactory;
    protected HierarchicalConfiguration config;
    protected ServletContext servletContext;
    protected ApplicationContext globalApplicationContext;
    protected ConfigurableApplicationContext applicationContext;
    protected URLClassLoader classLoader;
    protected UrlRewriter urlRewriter;
    protected Scheduler scheduler;
    protected GraphQLFactory graphQLFactory;
    protected SiteCacheWarmer cacheWarmer;

    protected ExecutorService maintenanceTaskExecutor;
    protected GraphQL graphQL;
    protected State state;

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
        // With this executor maintenance tasks are executed sequentially in the order they're received. This is
        // important when a cache warm is submitted and a GraphQL re-build needs to wait till the cache warm is
        // finished
        maintenanceTaskExecutor = Executors.newSingleThreadExecutor();
        state = State.CREATED;
    }

    public ContentStoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
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

    public String getInitScriptPath() {
        return initScriptPath;
    }

    public void setInitScriptPath(String initScriptPath) {
        this.initScriptPath = initScriptPath;
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

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
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

    public SiteCacheWarmer getCacheWarmer() {
        return cacheWarmer;
    }

    public void setCacheWarmer(SiteCacheWarmer cacheWarmer) {
        this.cacheWarmer = cacheWarmer;
    }

    public GraphQL getGraphQL() {
        return graphQL;
    }

    public boolean isValid() throws CrafterException {
        return state == State.INITIALIZED && storeService.validate(context);
    }

    public State getState() {
        return state;
    }

    public void init(boolean waitTillFinished) throws SiteContextInitializationException {
        if (state == State.CREATED) {
            publishEvent(new SiteContextCreatedEvent(this));

            Runnable initTask = () -> {
                SiteContext.setCurrent(this);
                try {
                    logger.info("--------------------------------------------------");
                    logger.info("<Initializing context site: " + siteName + ">");
                    logger.info("--------------------------------------------------");

                    if (cacheWarmer != null) {
                        cacheWarmer.warmUpCache(this, false);
                    }

                    buildGraphQLSchema();
                    executeInitScript();

                    state = State.INITIALIZED;

                    logger.info("--------------------------------------------------");
                    logger.info("</Initializing context site: " + siteName + ">");
                    logger.info("--------------------------------------------------");

                    publishEvent(new SiteContextInitializedEvent(this));
                } finally {
                    SiteContext.clear();
                }
            };

            if (waitTillFinished) {
                // Done through the executor so that maintenance tasks submitted while init are queued
                Future<?> future = maintenanceTaskExecutor.submit(initTask);
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new SiteContextInitializationException("Error while waiting for context init", e);
                }
            } else {
                maintenanceTaskExecutor.execute(initTask);
            }
        }
    }

    public void startCacheClear() {
        maintenanceTaskExecutor.execute(() -> {
            SiteContext.setCurrent(this);
            try {
                cacheClear();
            } finally {
                SiteContext.clear();
            }
        });
    }

    public void startGraphQLSchemaBuild() throws GraphQLBuildException {
        maintenanceTaskExecutor.execute(() -> {
            SiteContext.setCurrent(this);
            try {
                buildGraphQLSchema();
            } finally {
                SiteContext.clear();
            }
        });
    }

    public void destroy() throws CrafterException {
        state = State.DESTROYED;

        publishEvent(new SiteContextDestroyedEvent(this));

        maintenanceTaskExecutor.shutdownNow();

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

    protected void cacheClear() {
        publishEvent(new CacheClearStartedEvent(this));

        // If there's a cache warmer, do a content cache switch instead of aclear
        if (cacheWarmer != null) {
            cacheWarmer.warmUpCache(this, true);
            // Clear Freemarker cache
            freeMarkerConfig.getConfiguration().clearTemplateCache();
        } else {
            cacheService.clearScope(context);
            // Clear Freemarker cache
            freeMarkerConfig.getConfiguration().clearTemplateCache();
        }

        publishEvent(new CacheClearCompletedEvent(this));
    }

    protected void buildGraphQLSchema() {
        logger.info("Starting GraphQL schema build for site '{}'", siteName);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        publishEvent(new GraphQLBuildStartedEvent(this));

        try {
            GraphQL graphQL = graphQLFactory.getInstance(this);
            if (Objects.nonNull(graphQL)) {
                this.graphQL = graphQL;

                publishEvent(new GraphQLBuildCompletedEvent(this));
            }
        } catch (Exception e) {
            logger.error("Error building the GraphQL schema for site '" + siteName + "'", e);
        }

        stopWatch.stop();

        logger.info("GraphQL schema build completed for site '{}' in {} secs", siteName,
                    stopWatch.getTime(TimeUnit.SECONDS));
    }

    protected void executeInitScript() {
        if (storeService.exists(context, initScriptPath)) {
            try {
                Map<String, Object> variables = new HashMap<>();
                GroovyScriptUtils.addJobScriptVariables(variables, servletContext);

                logger.info("Executing init script for site '{}'", siteName);

                scriptFactory.getScript(initScriptPath).execute(variables);
            } catch (Exception e) {
                logger.error("Error executing init script for site '" + siteName + "'", e);
            }
        }
    }

    protected void publishEvent(SiteEvent event) {
        if (applicationContext != null) {
            applicationContext.publishEvent(event);
        } else {
            globalApplicationContext.publishEvent(event);
        }

        // Store a request attribute for the event so it's known later if the event was fired during the request
        RequestContext requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            requestContext.getRequest().setAttribute(event.getClass().getName(), event);
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
