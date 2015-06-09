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
import javax.annotation.PreDestroy;

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

    protected Map<String, SiteContext> contextRegistry;
    protected SiteContextFactory contextFactory;
    protected SiteContextFactory fallbackContextFactory;

    public SiteContextManager() {
        contextRegistry = new ConcurrentHashMap<>();
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
        for (Iterator<SiteContext> iter = contextRegistry.values().iterator(); iter.hasNext();) {
            iter.next().destroy();
            iter.remove();
        }

        logger.info("All site contexts have been destroyed");
    }

    public Collection<SiteContext> listContexts() {
        return contextRegistry.values();
    }

    public SiteContext getContext(String siteName, boolean fallback) {
        SiteContext context = contextRegistry.get(siteName);
        if (context == null) {
            synchronized (this) {
                // Double check locking, in case the context has been created already by another thread
                context = contextRegistry.get(siteName);
                if (context == null) {
                    if (fallback) {
                        context = fallbackContextFactory.createContext(siteName);
                        context.setFallback(true);
                    } else {
                        context = contextFactory.createContext(siteName);
                    }

                    contextRegistry.put(siteName, context);

                    logger.info("Site context created: " + context);
                }
            }
        }

        return context;
    }

    public synchronized void destroyContext(String siteName) {
        SiteContext context = contextRegistry.remove(siteName);
        if (context != null) {
            context.destroy();

            logger.info("Site context destroyed: " + context);
        }
    }

}
