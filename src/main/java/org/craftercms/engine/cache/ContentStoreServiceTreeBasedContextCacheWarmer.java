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
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.util.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@link ContextCacheWarmer} that performs warm up at the {@link ContentStoreService} level by preloading a
 * list of folder trees.
 *
 * @author avasquez
 * @since 3.1.4
 */
public class ContentStoreServiceTreeBasedContextCacheWarmer implements ContextCacheWarmer {

    private static final Logger logger = LoggerFactory.getLogger(ContentStoreServiceTreeBasedContextCacheWarmer.class);

    protected boolean warmUpEnabled;
    protected ContentStoreService contentStoreService;
    protected Map<String, Integer> preloadFolders;

    /**
     * Sets if warm up is enabled
     */
    @Required
    public void setWarmUpEnabled(boolean warmUpEnabled) {
        this.warmUpEnabled = warmUpEnabled;
    }

    /**
     * Sets the content store service
     */
    @Required
    public void setContentStoreService(ContentStoreService contentStoreService) {
        this.contentStoreService = contentStoreService;
    }

    /**
     * Sets the list of folder trees to preload in the cache. Each folder can have it's depth specifiedv
     * after a colon, like {@code PATH:DEPTH}
     */
    @Required
    public void setPreloadFolders(String[] preloadFolders) {
        this.preloadFolders = CacheUtils.parsePreloadFoldersList(preloadFolders);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warmUpCache(Context context) {
        for (Map.Entry<String, Integer> entry : preloadFolders.entrySet()) {
            String treeRoot = entry.getKey();
            int depth = entry.getValue();
            StopWatch stopWatch = new StopWatch();

            logger.info("Starting preload of tree [{}] with depth {}", treeRoot, depth);

            stopWatch.start();

            try {
                contentStoreService.getTree(context, treeRoot, depth);
            } catch (Exception e) {
                logger.error("Error while preloading tree at [{}]", treeRoot, e);
            }

            stopWatch.stop();

            logger.info("Preload of tree [{}] with depth {} completed in {} secs", treeRoot, depth,
                        stopWatch.getTime(TimeUnit.SECONDS));
        }
    }

}
