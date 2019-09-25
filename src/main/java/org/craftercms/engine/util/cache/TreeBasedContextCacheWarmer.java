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
package org.craftercms.engine.util.cache;

import org.craftercms.core.exception.ItemProcessingException;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.processors.impl.ItemProcessorPipeline;
import org.craftercms.core.service.*;
import org.craftercms.core.service.impl.CompositeItemFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * {@link ContextCacheWarmer} that uses the
 * {@link ContentStoreService#getTree(Context, CachingOptions, String, int, ItemFilter, ItemProcessor)} to cache
 * most of the site during warm up.
 *
 * @author avasquez
 * @since 3.1.4
 */
public class TreeBasedContextCacheWarmer implements ContextCacheWarmer {

    private static final Logger logger = LoggerFactory.getLogger(TreeBasedContextCacheWarmer.class);

    protected ContentStoreService storeService;
    protected String rootPath;
    protected int depth;
    protected CompositeItemFilter filters;
    protected ItemProcessorPipeline processors;

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Required
    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setFilters(List<ItemFilter> filters) {
        this.filters = new CompositeItemFilter(filters);
    }

    public void setProcessors(List<ItemProcessor> processors) {
        this.processors = new ItemProcessorPipeline(processors);
        this.processors.addProcessor(new WarmUpLoggingProcessor());
    }

    @Override
    public void warmUpCache(Context context) {
        storeService.getTree(context, null, rootPath, depth, filters, processors);
    }

    private static class WarmUpLoggingProcessor implements ItemProcessor {

        @Override
        public Item process(Context context, CachingOptions cachingOptions, Item item) throws ItemProcessingException {
            logger.debug("Warming up cache -> Adding item {}", item.getUrl());

            return item;
        }

    }

}
