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
import org.springframework.beans.factory.annotation.Required;

/**
 * Registry and lifecycle manager of {@link SiteContext}s.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextManager {

    private static final Log logger = LogFactory.getLog(SiteContextManager.class);

    protected Lock lock;
    protected Map<String, SiteContext> contextRegistry;
    protected SiteContextFactory contextFactory;
    protected SiteContextFactory fallbackContextFactory;

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
                    siteContext.destroy();

                    logger.info("Site context destroyed: " + siteContext);
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
                logger.info("==================================================");
                logger.info("<Destroying site context: " + siteName + ">");
                logger.info("==================================================");

                try {
                    destroyContext(siteName);
                } catch (Exception e) {
                    logger.error("Error destroying site context for site '" + siteName + "'", e);
                }

                logger.info("==================================================");
                logger.info("</Destroying site context: " + siteName + ">");
                logger.info("==================================================");
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
                siteContext.destroy();

                logger.info("Site context destroyed: " + siteContext);
            }
        } finally {
            lock.unlock();
        }
    }

    public SiteContext getContext(String siteName, boolean fallback) {
        SiteContext siteContext = contextRegistry.get(siteName);
        if (siteContext == null) {
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

                    contextRegistry.put(siteName, siteContext);

                    logger.info("Site context created: " + siteContext);
                    logger.info("==================================================");
                    logger.info("</Creating site context: " + siteName + ">");
                    logger.info("==================================================");
                }
            } finally {
                lock.unlock();
            }
        }

        return siteContext;
    }

}
