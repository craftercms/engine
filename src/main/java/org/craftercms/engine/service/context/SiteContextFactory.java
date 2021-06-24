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
package org.craftercms.engine.service.context;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.EncryptionAwareConfigurationReader;
import org.craftercms.commons.config.PublishingTargetResolver;
import org.craftercms.commons.spring.ApacheCommonsConfiguration2PropertySource;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.cache.SiteCacheWarmer;
import org.craftercms.engine.exception.SiteContextCreationException;
import org.craftercms.engine.graphql.GraphQLFactory;
import org.craftercms.engine.macro.MacroResolver;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.ScriptJobResolver;
import org.craftercms.engine.scripting.impl.GroovyScriptFactory;
import org.craftercms.engine.util.SchedulingUtils;
import org.craftercms.engine.util.config.SiteAwarePublishingTargetResolver;
import org.craftercms.engine.util.groovy.ContentStoreGroovyResourceLoader;
import org.craftercms.engine.util.groovy.ContentStoreResourceConnector;
import org.craftercms.engine.util.groovy.Dom4jExtension;
import org.craftercms.engine.util.quartz.JobContext;
import org.craftercms.engine.util.spring.ContentStoreResourceLoader;
import org.craftercms.engine.util.spring.context.RestrictedApplicationContext;
import org.jenkinsci.plugins.scriptsecurity.sandbox.Whitelist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.blacklists.Blacklist;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SandboxInterceptor;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.PermitAllWhitelist;
import org.quartz.Scheduler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.craftercms.engine.util.GroovyScriptUtils.getCompilerConfiguration;

/**
 * Factory for creating {@link SiteContext} with common properties. It also uses the {@link MacroResolver} to resolve
 * any macros specified in the {@code rootFolderPath} before creating the context (remember that macros can vary
 * between requests).
 *
 * @author Alfonso Vásquez
 */
@SuppressWarnings("rawtypes")
public class SiteContextFactory implements ApplicationContextAware, ServletContextAware {

    public static final String DEFAULT_SITE_NAME_MACRO_NAME = "siteName";
    public static final long DEFAULT_INIT_TIMEOUT = 300000L;
    public static final String CONFIG_BEAN_NAME = "siteConfig";
    public static final long DEFAULT_SHUTDOWN_TIMEOUT = 5;
    public static final String DEFAULT_PUBLISHING_TARGET_MACRO_NAME = "publishingTarget";
    public static final String CONFIG_KEY_ALLOWED_TEMPLATE_PATHS = "templates.allowed";

    private static final Log logger = LogFactory.getLog(SiteContextFactory.class);

    protected ServletContext servletContext;
    protected String siteNameMacroName;
    protected String storeType;
    protected String rootFolderPath;
    protected String staticAssetsPath;
    protected String templatesPath;
    protected String initScriptPath;
    protected String restScriptsPath;
    protected String controllerScriptsPath;
    protected String[] configPaths;
    protected String[] applicationContextPaths;
    protected String[] urlRewriteConfPaths;
    protected String[] proxyConfigPaths;
    protected String groovyClassesPath;
    protected Map<String, Object> groovyGlobalVars;
    protected boolean mergingOn;
    protected boolean cacheOn;
    protected int maxAllowedItemsInCache;
    protected boolean ignoreHiddenFiles;
    protected ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory;
    protected UrlTransformationEngine urlTransformationEngine;
    protected ContentStoreService storeService;
    protected CacheTemplate cacheTemplate;
    protected MacroResolver macroResolver;
    protected ApplicationContext globalApplicationContext;
    protected List<ScriptJobResolver> jobResolvers;
    protected Executor jobThreadPoolExecutor;
    protected GraphQLFactory graphQLFactory;
    protected boolean cacheWarmUpEnabled;
    protected SiteCacheWarmer cacheWarmer;
    protected long initTimeout;
    protected boolean disableVariableRestrictions;
    protected EncryptionAwareConfigurationReader configurationReader;
    protected String[] defaultPublicBeans;
    protected long shutdownTimeout;
    protected PublishingTargetResolver publishingTargetResolver;
    protected String publishingTargetMacroName;
    protected boolean enableScriptSandbox;
    protected boolean enableSandboxBlacklist;
    protected String sandboxBlacklist;
    protected boolean enableExpressions;

    public SiteContextFactory() {
        siteNameMacroName = DEFAULT_SITE_NAME_MACRO_NAME;
        mergingOn = Context.DEFAULT_MERGING_ON;
        cacheOn = Context.DEFAULT_CACHE_ON;
        maxAllowedItemsInCache = Context.DEFAULT_MAX_ALLOWED_ITEMS_IN_CACHE;
        ignoreHiddenFiles = Context.DEFAULT_IGNORE_HIDDEN_FILES;
        initTimeout = DEFAULT_INIT_TIMEOUT;
        defaultPublicBeans = new String[0];
        shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;
        publishingTargetMacroName = DEFAULT_PUBLISHING_TARGET_MACRO_NAME;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setSiteNameMacroName(String siteNameMacroName) {
        this.siteNameMacroName = siteNameMacroName;
    }

    @Required
    public void setStoreType(String storeType) {
        this.storeType = storeType;
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
    public void setInitScriptPath(String initScriptPath) {
        this.initScriptPath = initScriptPath;
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
    public void setConfigPaths(String[] configPaths) {
        this.configPaths = configPaths;
    }

    @Required
    public void setApplicationContextPaths(String[] applicationContextPaths) {
        this.applicationContextPaths = applicationContextPaths;
    }

    @Required
    public void setUrlRewriteConfPaths(String[] urlRewriteConfPaths) {
        this.urlRewriteConfPaths = urlRewriteConfPaths;
    }

    @Required
    public void setProxyConfigPaths(String[] proxyConfigPaths) {
        this.proxyConfigPaths = proxyConfigPaths;
    }

    @Required
    public void setGroovyClassesPath(String groovyClassesPath) {
        this.groovyClassesPath = groovyClassesPath;
    }

    @Required
    public void setGroovyGlobalVars(Map<String, Object> groovyGlobalVars) {
        this.groovyGlobalVars = groovyGlobalVars;
    }

    public void setMergingOn(boolean mergingOn) {
        this.mergingOn = mergingOn;
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

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Required
    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    @Required
    public void setJobResolvers(List<ScriptJobResolver> jobResolvers) {
        this.jobResolvers = jobResolvers;
    }

    @Required
    public void setJobThreadPoolExecutor(Executor jobThreadPoolExecutor) {
        this.jobThreadPoolExecutor = jobThreadPoolExecutor;
    }

    @Required
    public void setGraphQLFactory(GraphQLFactory graphQLFactory) {
        this.graphQLFactory = graphQLFactory;
    }

    @Required
    public void setCacheWarmUpEnabled(boolean cacheWarmUpEnabled) {
        this.cacheWarmUpEnabled = cacheWarmUpEnabled;
    }

    @Required
    public void setCacheWarmer(SiteCacheWarmer cacheWarmer) {
        this.cacheWarmer = cacheWarmer;
    }

    public void setInitTimeout(final long initTimeout) {
        this.initTimeout = initTimeout;
    }

    public void setDisableVariableRestrictions(boolean disableVariableRestrictions) {
        this.disableVariableRestrictions = disableVariableRestrictions;
    }

    @Required
    public void setConfigurationReader(EncryptionAwareConfigurationReader configurationReader) {
        this.configurationReader = configurationReader;
    }

    public void setDefaultPublicBeans(String[] defaultPublicBeans) {
        this.defaultPublicBeans = defaultPublicBeans;
    }

    public void setShutdownTimeout(long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    public void setPublishingTargetResolver(PublishingTargetResolver publishingTargetResolver) {
        this.publishingTargetResolver = publishingTargetResolver;
    }

    public void setEnableScriptSandbox(boolean enableScriptSandbox) {
        this.enableScriptSandbox = enableScriptSandbox;
    }

    public void setEnableSandboxBlacklist(boolean enableSandboxBlacklist) {
        this.enableSandboxBlacklist = enableSandboxBlacklist;
    }

    public void setSandboxBlacklist(String sandboxBlacklist) {
        this.sandboxBlacklist = sandboxBlacklist;
    }

    public void setEnableExpressions(boolean enableExpressions) {
        this.enableExpressions = enableExpressions;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.globalApplicationContext = applicationContext;
    }

    public SiteContext createContext(String siteName) {
        Map<String, String> macroValues = new HashMap<>();
        macroValues.put(siteNameMacroName, siteName);

        if (publishingTargetResolver instanceof SiteAwarePublishingTargetResolver) {
            String target = ((SiteAwarePublishingTargetResolver) publishingTargetResolver).getPublishingTarget(siteName);
            macroValues.put(publishingTargetMacroName, target);
        }

        String resolvedRootFolderPath = macroResolver.resolveMacros(rootFolderPath, macroValues);

        Context context = storeService.getContext(UUID.randomUUID().toString(), storeType, resolvedRootFolderPath,
                                                  mergingOn, cacheOn, maxAllowedItemsInCache, ignoreHiddenFiles);

        try {
            SiteContext siteContext = new SiteContext();
            siteContext.setInitTimeout(initTimeout);
            siteContext.setStoreService(storeService);
            siteContext.setCacheTemplate(cacheTemplate);
            siteContext.setSiteName(siteName);
            siteContext.setContext(context);
            siteContext.setStaticAssetsPath(staticAssetsPath);
            siteContext.setTemplatesPath(templatesPath);
            siteContext.setInitScriptPath(initScriptPath);
            siteContext.setFreeMarkerConfig(freeMarkerConfigFactory.getObject());
            siteContext.setUrlTransformationEngine(urlTransformationEngine);
            siteContext.setRestScriptsPath(restScriptsPath);
            siteContext.setControllerScriptsPath(controllerScriptsPath);
            siteContext.setGraphQLFactory(graphQLFactory);
            siteContext.setShutdownTimeout(shutdownTimeout);

            if (disableVariableRestrictions) {
                siteContext.setServletContext(servletContext);
            }

            if (cacheWarmUpEnabled) {
                siteContext.setCacheWarmer(cacheWarmer);
            }

            String[] resolvedConfigPaths = new String[configPaths.length];
            for (int i = 0; i < configPaths.length; i++) {
                resolvedConfigPaths[i] = macroResolver.resolveMacros(configPaths[i], macroValues);
            }

            String[] resolvedAppContextPaths = new String[applicationContextPaths.length];
            for (int i = 0; i < applicationContextPaths.length; i++) {
                resolvedAppContextPaths[i] = macroResolver.resolveMacros(applicationContextPaths[i], macroValues);
            }

            String[] resolvedUrlRewriteConfPaths = new String[urlRewriteConfPaths.length];
            for (int i = 0; i < urlRewriteConfPaths.length; i++) {
                resolvedUrlRewriteConfPaths[i] = macroResolver.resolveMacros(urlRewriteConfPaths[i], macroValues);
            }

            List<String> resolvedProxyConfPaths = Stream.of(proxyConfigPaths)
                    .map(path -> macroResolver.resolveMacros(path, macroValues))
                    .collect(toList());

            ResourceLoader resourceLoader = new ContentStoreResourceLoader(siteContext);
            HierarchicalConfiguration<?> config = getConfig(siteContext, resolvedConfigPaths, resourceLoader);

            configureScriptSandbox(siteContext, resourceLoader);
            URLClassLoader classLoader = getClassLoader(siteContext);
            ScriptFactory scriptFactory = getScriptFactory(siteContext, classLoader);
            ConfigurableApplicationContext appContext = getApplicationContext(siteContext, classLoader, config,
                                                                              resolvedAppContextPaths, resourceLoader);
            UrlRewriter urlRewriter = getUrlRewriter(siteContext, resolvedUrlRewriteConfPaths, resourceLoader);
            HierarchicalConfiguration proxyConfig = getProxyConfig(siteContext, resolvedProxyConfPaths, resourceLoader);

            siteContext.setScriptFactory(scriptFactory);
            siteContext.setConfig(config);
            siteContext.setGlobalApplicationContext(globalApplicationContext);
            siteContext.setApplicationContext(appContext);
            siteContext.setClassLoader(classLoader);
            siteContext.setUrlRewriter(urlRewriter);
            siteContext.setProxyConfig(proxyConfig);
            if (config != null) {
                siteContext.setAllowedTemplatePaths(config.getStringArray(CONFIG_KEY_ALLOWED_TEMPLATE_PATHS));
            }

            Scheduler scheduler = scheduleJobs(siteContext);
            siteContext.setScheduler(scheduler);

            return siteContext;
        } catch (Exception e) {
            logger.error("Error creating context for site '" + siteName + "'", e);

            // Destroy context if the site context creation failed
            storeService.destroyContext(context);

            throw e;
        }
    }

    protected HierarchicalConfiguration getConfig(SiteContext siteContext, String[] configPaths,
                                                  ResourceLoader resourceLoader) {
        String siteName = siteContext.getSiteName();

        logger.info("--------------------------------------------------");
        logger.info("<Loading configuration for site: " + siteName + ">");
        logger.info("--------------------------------------------------");

        try {
            for (int i = configPaths.length - 1; i >= 0; i--) {
                Resource config = resourceLoader.getResource(configPaths[i]);
                if (config.exists()) {
                    return configurationReader.readXmlConfiguration(config);
                }
            }
            return null;
        } catch (ConfigurationException e) {
            throw new SiteContextCreationException("Unable to load configuration for site '" + siteName + "'", e);
        } finally {
            logger.info("--------------------------------------------------");
            logger.info("</Loading configuration for site: " + siteName + ">");
            logger.info("--------------------------------------------------");
        }
    }

    protected void configureScriptSandbox(SiteContext siteContext, ResourceLoader resourceLoader) {
        try {
            // Enable both hardcoded & configurable blacklists
            if (enableScriptSandbox && enableSandboxBlacklist) {
                Resource sandboxBlacklist = resourceLoader.getResource(this.sandboxBlacklist);
                try (InputStream is = sandboxBlacklist.getInputStream()) {
                    Blacklist blacklist = new Blacklist(new InputStreamReader(is));
                    siteContext.scriptSandbox = new SandboxInterceptor(blacklist, singletonList(Dom4jExtension.class));
                }
            // Enable only the hardcoded blacklist
            } else if (enableScriptSandbox) {
                Whitelist whitelist = new PermitAllWhitelist();
                siteContext.scriptSandbox = new SandboxInterceptor(whitelist, singletonList(Dom4jExtension.class));
            }
        } catch (IOException e) {
            throw new SiteContextCreationException("Unable to load sandbox blacklist for site '" +
                    siteContext.getSiteName() + "'", e);
        }
    }

    protected URLClassLoader getClassLoader(SiteContext siteContext) {
        GroovyClassLoader classLoader =
                new GroovyClassLoader(getClass().getClassLoader(), getCompilerConfiguration(enableScriptSandbox));
        ContentStoreGroovyResourceLoader resourceLoader = new ContentStoreGroovyResourceLoader(siteContext,
                                                                                               groovyClassesPath);

        classLoader.setResourceLoader(resourceLoader);

        return classLoader;
    }

    protected ConfigurableApplicationContext getApplicationContext(SiteContext siteContext, URLClassLoader classLoader,
                                                                   HierarchicalConfiguration config,
                                                                   String[] applicationContextPaths,
                                                                   ResourceLoader resourceLoader) {
        String siteName = siteContext.getSiteName();

        logger.info("--------------------------------------------------");
        logger.info("<Loading application context for site: " + siteName + ">");
        logger.info("--------------------------------------------------");

        try {
            Resource appContextResource = null;

            for (int i = applicationContextPaths.length - 1; i >= 0; i--) {
                Resource resource = resourceLoader.getResource(applicationContextPaths[i]);
                if (resource.exists()) {
                    appContextResource = resource;
                    break;
                }
            }

            if (appContextResource != null) {
                GenericApplicationContext appContext;
                if (disableVariableRestrictions) {
                    appContext = new GenericApplicationContext(globalApplicationContext);
                } else {
                    appContext = new RestrictedApplicationContext(globalApplicationContext, defaultPublicBeans);
                }
                appContext.setClassLoader(classLoader);

                if (!enableExpressions) {
                    appContext.addBeanFactoryPostProcessor(factory -> factory.setBeanExpressionResolver(null));
                }

                if (config != null) {
                    MutablePropertySources propertySources = appContext.getEnvironment().getPropertySources();
                    propertySources.addFirst(new ApacheCommonsConfiguration2PropertySource(CONFIG_BEAN_NAME, config));
                    appContext.getBeanFactory().registerSingleton(CONFIG_BEAN_NAME, config);
                }

                XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(appContext);
                reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);

                reader.loadBeanDefinitions(appContextResource);

                appContext.refresh();

                return appContext;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new SiteContextCreationException("Unable to load application context for site '" + siteName + "'", e);
        } finally {
            logger.info("--------------------------------------------------");
            logger.info("</Loading application context for site: " + siteName + ">");
            logger.info("--------------------------------------------------");
        }
    }

    protected UrlRewriter getUrlRewriter(SiteContext siteContext, String[] urlRewriteConfPaths,
                                         ResourceLoader resourceLoader) {
        String siteName = siteContext.getSiteName();
        String confPath = null;
        Resource confResource = null;
        Conf conf = null;
        UrlRewriter urlRewriter = null;

        logger.info("--------------------------------------------------");
        logger.info("<Loading URL rewrite engine for site: " + siteName + ">");
        logger.info("--------------------------------------------------");

        try {
            for (int i = urlRewriteConfPaths.length - 1; i >= 0; i--) {
                Resource resource = resourceLoader.getResource(urlRewriteConfPaths[i]);
                if (resource.exists()) {
                    confPath = urlRewriteConfPaths[i];
                    confResource = resource;
                    break;
                }
            }

            if (confResource != null) {
                // By convention, if it ends in .xml, it's an XML-style url rewrite config, else it's a mod_rewrite-style
                // url rewrite config
                boolean modRewriteStyleConf = !confPath.endsWith(".xml");

                try (InputStream is = confResource.getInputStream()) {
                    conf = new Conf(servletContext, is, confPath, "", modRewriteStyleConf);

                    logger.info("URL rewrite configuration loaded @ " + confResource);
                }
            }

            if (conf != null) {
                if (conf.isOk() && conf.isEngineEnabled()) {
                    urlRewriter = new UrlRewriter(conf);

                    logger.info("URL rewrite engine loaded for site " + siteName + " (conf ok)");
                } else {
                    logger.error("URL rewrite engine not loaded, there might have been conf errors");
                }
            }

            return urlRewriter;
        } catch (Exception e) {
            throw new SiteContextCreationException("Unable to load URL rewrite conf for site '" + siteName + "'", e);
        } finally {
            logger.info("--------------------------------------------------");
            logger.info("</Loading URL rewrite engine for site: " + siteName + ">");
            logger.info("--------------------------------------------------");
        }
    }

    protected HierarchicalConfiguration getProxyConfig(SiteContext siteContext, List<String> configPaths,
                                                       ResourceLoader resourceLoader) {
        String siteName = siteContext.getSiteName();

        logger.info("-------------------------------------------------------");
        logger.info("<Loading proxy configuration for site: " + siteName + ">");
        logger.info("-------------------------------------------------------");

        try {
            ListIterator<String> iterator = configPaths.listIterator(configPaths.size());
            while(iterator.hasPrevious()) {
                Resource resource = resourceLoader.getResource(iterator.previous());
                if (resource.exists()) {
                    return configurationReader.readXmlConfiguration(resource);
                }
            }
            return null;
        } catch (ConfigurationException e) {
            throw new SiteContextCreationException("Unable to load proxy configuration for site '" + siteName + "'", e);
        } finally {
            logger.info("---------------------------------------------------------");
            logger.info("</Loading proxy configuration for site: " + siteName + ">");
            logger.info("---------------------------------------------------------");
        }
    }

    protected ScriptFactory getScriptFactory(SiteContext siteContext, URLClassLoader classLoader) {
        return new GroovyScriptFactory(siteContext, new ContentStoreResourceConnector(siteContext), classLoader,
                                       groovyGlobalVars, enableScriptSandbox);
    }

    protected Scheduler scheduleJobs(SiteContext siteContext) {
        String siteName = siteContext.getSiteName();

        logger.info("--------------------------------------------------");
        logger.info("<Scheduling job scripts for site: " + siteName + ">");
        logger.info("--------------------------------------------------");

        try {
            List<JobContext> allJobContexts = new ArrayList<>();

            for (ScriptJobResolver jobResolver : jobResolvers) {
                List<JobContext> jobContexts = jobResolver.resolveJobs(siteContext);
                if (CollectionUtils.isNotEmpty(jobContexts)) {
                    allJobContexts.addAll(jobContexts);
                }
            }

            if (CollectionUtils.isNotEmpty(allJobContexts)) {
                Scheduler scheduler = SchedulingUtils.createScheduler(
                        String.format("%s_%s_scheduler", siteName, siteContext.getContext().getId()),
                        jobThreadPoolExecutor);

                for (JobContext jobContext : allJobContexts) {
                    scheduler.scheduleJob(jobContext.getDetail(), jobContext.getTrigger());

                    logger.info("Scheduled job: " + jobContext + " for site '" + siteName + "'");
                }

                scheduler.start();

                return scheduler;
            }
        } catch (Exception e) {
            logger.error("Unable to schedule jobs for site '" + siteName + "'", e);
        } finally {
            logger.info("--------------------------------------------------");
            logger.info("</Scheduling job scripts for site: " + siteName + ">");
            logger.info("--------------------------------------------------");
        }

        return null;
    }

}
