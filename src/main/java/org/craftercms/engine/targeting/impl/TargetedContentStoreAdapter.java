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
package org.craftercms.engine.targeting.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.AuthenticationException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.core.exception.StoreException;
import org.craftercms.core.exception.XmlFileParseException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.targeting.CandidateTargetedUrlsResolver;
import org.craftercms.engine.util.TargetingUtils;
import org.craftercms.engine.properties.SiteProperties;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link ContentStoreAdapter} implementation that uses a {@link CandidateTargetedUrlsResolver} to generate candidate
 * URLs for targeted content lookup. For example, if an item at /site/website/es_CR/products/index.xml is requested,
 * the adapter might try to find the content first at that URL, then at /site/website/es/products/index.xml and
 * finally at /site/website/en/products/index.xml
 *
 * <p>The adapter is also capable of merging the items of folders that belong to the same family of targeted content,
 * so for example, the tree of /site/website/es_CR can be the combination of all the items at /site/website/es_CR,
 * /site/website/es and /site/website/en.</p>
 *
 * @author avasquez
 */
public class TargetedContentStoreAdapter implements ContentStoreAdapter {

    public static final Log logger = LogFactory.getLog(TargetedContentStoreAdapter.class);

    protected ContentStoreAdapter actualAdapter;
    protected CandidateTargetedUrlsResolver candidateTargetedUrlsResolver;

    @Required
    public void setCandidateTargetedUrlsResolver(CandidateTargetedUrlsResolver candidateTargetedUrlsResolver) {
        this.candidateTargetedUrlsResolver = candidateTargetedUrlsResolver;
    }

    @Required
    public void setActualAdapter(ContentStoreAdapter actualAdapter) {
        this.actualAdapter = actualAdapter;
    }

    @Override
    public Context createContext(String id, String storeServerUrl, String username, String password, String rootFolderPath,
                                 boolean mergingOn, boolean cacheOn, int maxAllowedItemsInCache,
                                 boolean ignoreHiddenFiles) throws StoreException, AuthenticationException {
        Context context = actualAdapter.createContext(id, storeServerUrl, username, password, rootFolderPath,
                                                      mergingOn, cacheOn, maxAllowedItemsInCache, ignoreHiddenFiles);

        return new ContextWrapper(this, context);
    }

    @Override
    public boolean validate(Context context) throws StoreException, AuthenticationException {
        return actualAdapter.validate(((ContextWrapper)context).getActualContext());
    }

    @Override
    public void destroyContext(
        Context context) throws InvalidContextException, StoreException, AuthenticationException {
        context = ((ContextWrapper)context).getActualContext();

        actualAdapter.destroyContext(context);
    }

    @Override
    public boolean exists(Context context, String path) throws InvalidContextException, StoreException {
        context = ((ContextWrapper)context).getActualContext();

        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(path)) {
            List<String> candidatePaths = candidateTargetedUrlsResolver.getUrls(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    if (actualAdapter.exists(context, candidatePath)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Targeted of " + path + " found at " + candidatePath);
                        }

                        return true;
                    }
                }

                return false;
            } else {
                return actualAdapter.exists(context, path);
            }
        } else {
            return actualAdapter.exists(context, path);
        }
    }

    @Override
    public Content findContent(Context context, CachingOptions cachingOptions,
                               String path) throws InvalidContextException, StoreException {
        context = ((ContextWrapper)context).getActualContext();

        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(path)) {
            List<String> candidatePaths = candidateTargetedUrlsResolver.getUrls(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    Content content = actualAdapter.findContent(context, cachingOptions, candidatePath);
                    if (content != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Targeted version of " + path + " found at " + candidatePath);
                        }

                        return content;
                    }
                }

                return null;
            } else {
                return actualAdapter.findContent(context, cachingOptions, path);
            }
        } else {
            return actualAdapter.findContent(context, cachingOptions, path);
        }
    }

    @Override
    public Item findItem(Context context, CachingOptions cachingOptions, String path,
                         boolean withDescriptor) throws InvalidContextException, XmlFileParseException, StoreException {
        context = ((ContextWrapper)context).getActualContext();

        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(path)) {
            List<String> candidatePaths = candidateTargetedUrlsResolver.getUrls(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    Item item = actualAdapter.findItem(context, cachingOptions, candidatePath, withDescriptor);
                    if (item != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Targeted version of " + path + " found at " + candidatePath);
                        }

                        return item;
                    }
                }

                return null;
            } else {
                return actualAdapter.findItem(context, cachingOptions, path, withDescriptor);
            }
        } else {
            return actualAdapter.findItem(context, cachingOptions, path, withDescriptor);
        }
    }

    @Override
    public List<Item> findItems(Context context, CachingOptions cachingOptions, String path,
                                boolean withDescriptor) throws InvalidContextException, XmlFileParseException,
        StoreException {
        context = ((ContextWrapper)context).getActualContext();

        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(path)) {
            List<String> candidatePaths = candidateTargetedUrlsResolver.getUrls(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                if (SiteProperties.isMergeFolders()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Merging child items of " + candidatePaths);
                    }

                    List<Item> mergedItems = null;

                    for (String candidatePath : candidatePaths) {
                        List<Item> items = actualAdapter.findItems(context, cachingOptions, candidatePath,
                                                                   withDescriptor);
                        mergedItems = mergeItems(mergedItems, items);
                    }

                    return mergedItems;
                } else {
                    for (String candidatePath : candidatePaths) {
                        List<Item> items = actualAdapter.findItems(context, cachingOptions, candidatePath,
                                                                   withDescriptor);
                        if (CollectionUtils.isNotEmpty(items)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Targeted version of " + path + " found at " + candidatePath);
                            }

                            return items;
                        }
                    }

                    return null;
                }
            } else {
                return actualAdapter.findItems(context, cachingOptions, path, withDescriptor);
            }
        } else {
            return actualAdapter.findItems(context, cachingOptions, path, withDescriptor);
        }
    }

    protected List<Item> mergeItems(List<Item> overriding, List<Item> original) {
        if (overriding == null) {
            return original;
        } else if (original == null) {
            return overriding;
        } else {
            List<Item> merged = new CachingAwareList<>(new ArrayList<>(overriding));

            for (Item item : original) {
                if (!containsItem(merged, item)) {
                    merged.add(item);
                }
            }

            return merged;
        }
    }

    protected boolean containsItem(final List<Item> list, final Item item) {
        int idx = ListUtils.indexOf(list, new Predicate<Item>() {

            @Override
            public boolean evaluate(Item it) {
                return it.getName().equals(item.getName());
            }

        });

        return idx >= 0;
    }

    protected static class ContextWrapper implements Context {

        private TargetedContentStoreAdapter storeAdapter;
        private Context actualContext;

        public ContextWrapper(TargetedContentStoreAdapter storeAdapter, Context actualContext) {
            this.storeAdapter = storeAdapter;
            this.actualContext = actualContext;
        }

        public Context getActualContext() {
            return actualContext;
        }

        @Override
        public String getId() {
            return actualContext.getId();
        }

        @Override
        public ContentStoreAdapter getStoreAdapter() {
            return storeAdapter;
        }

        @Override
        public String getStoreServerUrl() {
            return actualContext.getStoreServerUrl();
        }

        @Override
        public String getRootFolderPath() {
            return actualContext.getRootFolderPath();
        }

        @Override
        public boolean isMergingOn() {
            return actualContext.isMergingOn();
        }

        @Override
        public boolean isCacheOn() {
            return actualContext.isCacheOn();
        }

        @Override
        public int getMaxAllowedItemsInCache() {
            return actualContext.getMaxAllowedItemsInCache();
        }

        @Override
        public boolean ignoreHiddenFiles() {
            return actualContext.ignoreHiddenFiles();
        }

        @Override
        public String toString() {
            return actualContext.toString();
        }

    }

}
