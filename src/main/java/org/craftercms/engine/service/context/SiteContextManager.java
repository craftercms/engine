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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.model.Module;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.core.exception.RootFolderNotFoundException;
import org.craftercms.engine.event.SiteContextCreatedEvent;
import org.craftercms.engine.event.SiteContextDestroyedEvent;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

/**
 * Registry and lifecycle manager of {@link SiteContext}s.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextManager implements ApplicationContextAware {

    private static final Log logger = LogFactory.getLog(SiteContextManager.class);

    protected Lock lock;
    protected Map<String, SiteContext> contextRegistry;
    protected SiteContextFactory contextFactory;
    protected SiteContextFactory fallbackContextFactory;
    protected ApplicationContext applicationContext;
    protected EntitlementValidator entitlementValidator;

    public SiteContextManager() {
        lock = new ReentrantLock();
        contextRegistry = new ConcurrentHashMap<>();
    }

    public Lock getLock() {
        return lock;
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
    public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PreDestroy
    public void destroy() {
        destroyAllContexts();
    }

    public Collection<SiteContext> listContexts() {
        return contextRegistry.values();
    }

    public void createContexts(Collection<String> siteNames) {
        logger.info("==================================================");
        logger.info("<CREATING SITE CONTEXTS>");
        logger.info("==================================================");

        if (CollectionUtils.isNotEmpty(siteNames)) {
            for (String siteName : siteNames) {
                try {
                    // If the site context doesn't exist (it's new), it will be created
                    createContext(siteName, false);
                } catch (Exception e) {
                    logger.error("Error creating site context for site '" + siteName + "'", e);
                }
            }
        }

        logger.info("==================================================");
        logger.info("</CREATING SITE CONTEXTS>");
        logger.info("==================================================");
    }

    public SiteContext createContext(String siteName, boolean fallback) {
        return getContext(siteName, fallback);
    }

    public void destroyAllContexts() {
        logger.info("==================================================");
        logger.info("<DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");

        lock.lock();
        try {
            for (Iterator<SiteContext> iter = contextRegistry.values().iterator(); iter.hasNext();) {
                SiteContext siteContext = iter.next();
                String siteName = siteContext.getSiteName();

                logger.info("==================================================");
                logger.info("<Destroying site context: " + siteName + ">");
                logger.info("==================================================");

                try {
                    destroyContext(siteContext);
                } catch (Exception e) {
                    logger.error("Error destroying site context for site '" + siteName + "'", e);
                }

                logger.info("==================================================");
                logger.info("</Destroying site context: " + siteName + ">");
                logger.info("==================================================");

                iter.remove();
            }
        } finally {
            lock.unlock();
        }

        logger.info("==================================================");
        logger.info("</DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");
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

    public void destroyContext(String siteName) {
        lock.lock();
        try {
            SiteContext siteContext = contextRegistry.remove(siteName);
            if (siteContext != null) {
                logger.info("==================================================");
                logger.info("<Destroying site context: " + siteName + ">");
                logger.info("==================================================");

                destroyContext(siteContext);

                logger.info("==================================================");
                logger.info("</Destroying site context: " + siteName + ">");
                logger.info("==================================================");
            }
        } finally {
            lock.unlock();
        }
    }

    public SiteContext getContext(String siteName, boolean fallback) {
        SiteContext siteContext = contextRegistry.get(siteName);
        if (siteContext == null) {
            if(!fallback) {
                try {
                    int totalSites = (int) contextRegistry.values().stream()
                                            .filter(context -> !context.isFallback())
                                            .count();
                    entitlementValidator.validateEntitlement(Module.ENGINE, EntitlementType.SITE, totalSites, 1);
                } catch (EntitlementException e) {
                    return null;
                }
            }
            lock.lock();
            try {
                // Double check locking, in case the context has been created already by another thread
                siteContext = contextRegistry.get(siteName);
                if (siteContext == null) {
                    logger.info("==================================================");
                    logger.info("<Creating site context: " + siteName + ">");
                    logger.info("==================================================");

                    if (fallback) {
                        siteContext = fallbackContextFactory.createContext(siteName);
                        siteContext.setFallback(true);
                    } else {
                        siteContext = contextFactory.createContext(siteName);
                    }

                    publishEvent(new SiteContextCreatedEvent(siteContext, this), siteContext);

                    contextRegistry.put(siteName, siteContext);

                    logger.info("Site context created: " + siteContext);
                    logger.info("==================================================");
                    logger.info("</Creating site context: " + siteName + ">");
                    logger.info("==================================================");
                }
            } catch (RootFolderNotFoundException e) {
                logger.error("Cannot resolve root folder for site '" + siteName + "'", e);
            } finally {
                lock.unlock();
            }
        } else {
            if (!siteContext.isValid()) {
                logger.error("Site context " + siteContext + " is not valid anymore");

                destroyContext(siteName);

                siteContext = null;
            }
        }

        return siteContext;
    }

    protected void destroyContext(SiteContext siteContext) {
        publishEvent(new SiteContextDestroyedEvent(siteContext, this), siteContext);

        siteContext.destroy();

        logger.info("Site context destroyed: " + siteContext);
    }

    protected void publishEvent(ApplicationEvent event, SiteContext siteContext) {
        ApplicationContext siteApplicationContext = siteContext.getApplicationContext();
        if (siteApplicationContext != null) {
            siteApplicationContext.publishEvent(event);
        } else {
            applicationContext.publishEvent(event);
        }
    }

}
