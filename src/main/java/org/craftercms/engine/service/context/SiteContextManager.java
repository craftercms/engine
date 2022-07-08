/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.concurrent.locks.KeyBasedLockFactory;
import org.craftercms.commons.concurrent.locks.WeakKeyBasedReentrantLockFactory;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.engine.event.SiteContextPurgedEvent;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Registry and lifecycle manager of {@link SiteContext}s.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextManager implements ApplicationContextAware, DisposableBean {

    private static final Log logger = LogFactory.getLog(SiteContextManager.class);

    protected ApplicationContext applicationContext;
    protected KeyBasedLockFactory<ReentrantLock> siteLockFactory;
    protected Map<String, SiteContext> contextRegistry;
    protected SiteContextFactory contextFactory;
    protected SiteContextFactory fallbackContextFactory;
    protected SiteListResolver siteListResolver;
    protected EntitlementValidator entitlementValidator;
    protected boolean waitForContextInit;
    protected Executor jobThreadPoolExecutor;
    protected String defaultSiteName;

    public SiteContextManager() {
        siteLockFactory = new WeakKeyBasedReentrantLockFactory();
        contextRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Required
    public void setContextFactory(SiteContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Required
    public void setFallbackContextFactory(SiteContextFactory fallbackContextFactory) {
        this.fallbackContextFactory = fallbackContextFactory;
    }

    @Required
    public void setSiteListResolver(final SiteListResolver siteListResolver) {
        this.siteListResolver = siteListResolver;
    }

    @Required
    public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    @Required
    public void setWaitForContextInit(boolean waitForContextInit) {
        this.waitForContextInit = waitForContextInit;
    }

    @Required
    public void setJobThreadPoolExecutor(Executor jobThreadPoolExecutor) {
        this.jobThreadPoolExecutor = jobThreadPoolExecutor;
    }

    @Required
    public void setDefaultSiteName(final String defaultSiteName) {
        this.defaultSiteName = defaultSiteName;
    }

    public void destroy() {
        destroyAllContexts();
    }

    public Collection<SiteContext> listContexts() {
        return contextRegistry.values();
    }

    /**
     * Creates all contexts (if not already created) from the site list resolver
     *
     * @param concurrent if the context creation should be done concurrently
     */
    public void createContexts(boolean concurrent) {
        Collection<String> siteNames = siteListResolver.getSiteList();

        logger.info("==================================================");
        logger.info("<CREATING SITE CONTEXTS>");
        logger.info("==================================================");

        if (CollectionUtils.isNotEmpty(siteNames)) {
            if (concurrent) {
                CompletionService<SiteContext> cs = new ExecutorCompletionService<>(jobThreadPoolExecutor);
                for (String siteName : siteNames) {
                    cs.submit(() -> {
                        try {
                            // If the site context doesn't exist (it's new), it will be created
                            return getContext(siteName, false);
                        } catch (Exception e) {
                            logger.error("Error creating site context for site '" + siteName + "'", e);
                        }

                        return null;
                    });
                }

                for (int i = 0; i < siteNames.size(); i++) {
                    try {
                        cs.take();
                    } catch (InterruptedException e) {
                        logger.error("Stopping creation of site contexts, thread interrupted");
                        return;
                    }
                }
            } else {
                for (String siteName : siteNames) {
                    try {
                        // If the site context doesn't exist (it's new), it will be created
                        getContext(siteName, false);
                    } catch (Exception e) {
                        logger.error("Error creating site context for site '" + siteName + "'", e);
                    }
                }
            }
        }

        logger.info("==================================================");
        logger.info("</CREATING SITE CONTEXTS>");
        logger.info("==================================================");
    }

    public void syncContexts() {
        logger.debug("Syncing the site contexts ...");

        Collection<String> siteNames = siteListResolver.getSiteList();

        // destroy the contexts for sites in the registry that are not present anymore (except fallback sites)
        contextRegistry.forEach((siteName, siteContext) -> {
            if (!siteContext.isFallback() && !siteNames.contains(siteName)) {
                try {
                    destroyContext(siteName);
                } catch (Exception e) {
                    logger.error("Error destroying site context for site '" + siteName + "'", e);
                }
            }
        });

        // create the contexts for new sites
        siteNames.forEach(siteName -> {
            try {
                getContext(siteName, false);
            } catch (Exception e) {
                logger.error("Error creating site context for site '" + siteName + "'", e);
            }
        });
    }

    /**
     * Destroys all contexts
     */
    public void destroyAllContexts() {
        logger.info("==================================================");
        logger.info("<DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");


        for (Iterator<SiteContext> iter = contextRegistry.values().iterator(); iter.hasNext();) {
            SiteContext siteContext = iter.next();
            String siteName = siteContext.getSiteName();

            logger.info("==================================================");
            logger.info("<Destroying site context: " + siteName + ">");
            logger.info("==================================================");

            Lock lock = siteLockFactory.getLock(siteName);
            lock.lock();
            try {
                destroyContext(siteContext);
            } catch (Exception e) {
                logger.error("Error destroying site context for site '" + siteName + "'", e);
            } finally {
                lock.unlock();
            }

            logger.info("==================================================");
            logger.info("</Destroying site context: " + siteName + ">");
            logger.info("==================================================");

            iter.remove();
        }

        logger.info("==================================================");
        logger.info("</DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");
    }

    /**
     * Gets the {@link SiteContext} for the specified site name. If no context exists, a new one is created.
     *
     * @param siteName the context's site name
     * @param fallback if the context is a fallback (which means it will be used if no context can be resolved during
     *                 requests
     *
     * @return the context
     */
    public SiteContext getContext(String siteName, boolean fallback) {
        SiteContext siteContext = contextRegistry.get(siteName);
        if (siteContext == null) {
            if (!fallback && !siteName.equals(defaultSiteName) && !validateSiteCreationEntitlement()) {
                return null;
            }

            Lock lock = siteLockFactory.getLock(siteName);
            lock.lock();
            try {
                // Double check locking, in case the context has been created already by another thread
                siteContext = contextRegistry.get(siteName);
                if (siteContext == null) {
                    logger.info("==================================================");
                    logger.info("<Creating site context: " + siteName + ">");
                    logger.info("==================================================");

                    siteContext = createContext(siteName, fallback);

                    logger.info("==================================================");
                    logger.info("</Creating site context: " + siteName + ">");
                    logger.info("==================================================");
                }
            } finally {
                lock.unlock();
            }
        } else if (!siteContext.isValid()) {
            logger.error("Site context " + siteContext + " is not valid anymore");

            destroyContext(siteName);

            siteContext = null;
        }

        return siteContext;
    }

    /**
     * Starts a context rebuild in the background
     *
     * @param siteName the site name of the context
     * @param fallback if the new context should be a fallback context
     */
    public void startContextRebuild(String siteName, boolean fallback) {
        startContextRebuild(siteName, fallback, null);
    }

    /**
     * Starts a context rebuild in the background
     *
     * @param siteName the site name of the context
     * @param fallback if the new context should be a fallback context
     * @param callback function to call with the new context after it has been rebuilt
     */
    public void startContextRebuild(String siteName, boolean fallback, Consumer<SiteContext> callback) {
        jobThreadPoolExecutor.execute(() -> {
            SiteContext siteContext = rebuildContext(siteName, fallback);
            if (callback != null ){
                callback.accept(siteContext);
            }
        });
    }

    /**
     * Starts a destroy context in the background
     *
     * @param siteName the site name of the context
     */
    public void startDestroyContext(String siteName) {
        jobThreadPoolExecutor.execute(() -> destroyContext(siteName));
    }

    /**
     * Destroys the context for the specified site name, and removes it from the registry, effectively purging it
     *
     * @param siteName the site name of the context to destroy
     */
    protected void destroyContext(String siteName) {
        SiteContext siteContext;
        Lock lock = siteLockFactory.getLock(siteName);
        lock.lock();
        try {
            siteContext = contextRegistry.remove(siteName);
        } finally {
            lock.unlock();
        }

        if (siteContext != null) {
            logger.info("==================================================");
            logger.info("<Destroying site context: " + siteName + ">");
            logger.info("==================================================");

            try {
                destroyContext(siteContext);
            } finally {
                applicationContext.publishEvent(new SiteContextPurgedEvent(siteContext));
            }

            logger.info("==================================================");
            logger.info("</Destroying site context: " + siteName + ">");
            logger.info("==================================================");
        }
    }

    protected void destroyContexts(Collection<String> siteNames) {
        logger.info("==================================================");
        logger.info("<DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");

        if (CollectionUtils.isNotEmpty(siteNames)) {
            for (String siteName : siteNames) {
                try {
                    destroyContext(siteName);
                } catch (Exception e) {
                    logger.error("Error destroying site context for site '" + siteName + "'", e);
                }
            }
        }

        logger.info("==================================================");
        logger.info("</DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");
    }

    protected SiteContext createContext(String siteName, boolean fallback) {
        SiteContext siteContext;

        if (fallback) {
            siteContext = fallbackContextFactory.createContext(siteName);
            siteContext.setFallback(true);
        } else {
            siteContext = contextFactory.createContext(siteName);
        }

        siteContext.init(waitForContextInit);

        contextRegistry.put(siteName, siteContext);

        logger.info("Site context created: " + siteContext);

        return siteContext;
    }

    protected SiteContext rebuildContext(String siteName, boolean fallback) {
        Lock lock = siteLockFactory.getLock(siteName);
        lock.lock();
        try {
            logger.info("==================================================");
            logger.info("<Rebuilding site context: " + siteName + ">");
            logger.info("==================================================");

            SiteContext oldSiteContext = contextRegistry.get(siteName);
            SiteContext newContext = createContext(siteName, fallback);

            oldSiteContext.destroy();

            logger.info("==================================================");
            logger.info("</Rebuilding site context: " + siteName + ">");
            logger.info("==================================================");

            return newContext;
        } finally {
            lock.unlock();
        }
    }

    protected void destroyContext(SiteContext siteContext) {
        siteContext.destroy();

        logger.info("Site context destroyed: " + siteContext);
    }

    protected boolean validateSiteCreationEntitlement() {
        try {
            entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
            return true;
        } catch (EntitlementException e) {
            return false;
        }
    }

}
