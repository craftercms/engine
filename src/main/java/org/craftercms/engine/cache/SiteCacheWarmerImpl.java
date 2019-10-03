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
package org.craftercms.engine.cache;

import org.apache.commons.lang3.time.StopWatch;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.service.context.SiteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation for {@link SiteCacheWarmerImpl}.
 *
 * @author avasquez
 * @since 3.1.4
 */
public class SiteCacheWarmerImpl implements SiteCacheWarmer {

    private static final Logger logger = LoggerFactory.getLogger(SiteCacheWarmerImpl.class);

    protected CacheService cacheService;
    protected List<ContextCacheWarmer> contextCacheWarmers;

    /**
     * Sets the {@link CacheService}
     */
    @Required
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Sets the list of {@link ContextCacheWarmer} used to perfotm the warm up
     */
    @Required
    public void setContextCacheWarmers(List<ContextCacheWarmer> contextCacheWarmers) {
        this.contextCacheWarmers = contextCacheWarmers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warmUpCache(SiteContext siteContext, boolean switchCache) {
        String siteName = siteContext.getSiteName();
        StopWatch stopWatch = new StopWatch();

        if (switchCache) {
            Context currentContext = siteContext.getContext();
            long oldCacheVersion = currentContext.getCacheVersion();
            long newCacheVersion = System.nanoTime();

            // Create a tmp context that will be used to warm up a new version of the cache
            Context tmpContext = currentContext.clone();
            tmpContext.setCacheVersion(newCacheVersion);

            cacheService.addScope(tmpContext);

            try {
                logger.info("Starting warm up for new cache of site '{}'", siteName);

                stopWatch.start();

                doCacheWarmUp(tmpContext);
                if (siteContext.isValid()) {
                    // Switch cache versions
                    currentContext.setCacheVersion(newCacheVersion);
                    tmpContext.setCacheVersion(oldCacheVersion);

                    // Delete old cache version
                    cacheService.removeScope(tmpContext);
                } else {
                    throw new CrafterException("The site context has become invalid (possibly destroyed)");
                }

                stopWatch.stop();

                logger.info("Warm up for new cache of site '{}' completed (switched with old cache) in {} secs",
                            siteName, stopWatch.getTime(TimeUnit.SECONDS));
            } catch (Exception e) {
                cacheService.removeScope(tmpContext);

                logger.error("Cache warm up failed", e);
            }
        } else {
            logger.info("Starting warm up for cache of site '{}'", siteName);

            stopWatch.start();

            doCacheWarmUp(siteContext.getContext());

            stopWatch.stop();

            logger.info("Warm up for cache of site '{}' completed in {} secs", siteName,
                        stopWatch.getTime(TimeUnit.SECONDS));
        }
    }

    private void doCacheWarmUp(Context cacheContext) {
        for (ContextCacheWarmer cacheWarmer : contextCacheWarmers) {
            cacheWarmer.warmUpCache(cacheContext);
        }
    }

}
