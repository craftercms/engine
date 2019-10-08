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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
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
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.targeting.CandidateTargetedUrlsResolver;
import org.craftercms.engine.util.TargetingUtils;
import org.craftercms.engine.util.store.decorators.ContentStoreAdapterDecorator;
import org.craftercms.engine.util.store.decorators.DecoratedStoreAdapterContext;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ContentStoreAdapterDecorator} that uses a {@link CandidateTargetedUrlsResolver} to generate candidate
 * URLs for targeted content lookup. For example, if an item at /site/website/es_CR/products/index.xml is requested,
 * the adapter might try to find the content first at that URL, then at /site/website/es/products/index.xml and
 * finally at /site/website/en/products/index.xml
 *
 * <p>The decorator is also capable of merging the items of folders that belong to the same family of targeted content,
 * so for example, the tree of /site/website/es_CR can be the combination of all the items at /site/website/es_CR,
 * /site/website/es and /site/website/en.</p>
 *
 * @author avasquez
 */
public class TargetedContentStoreAdapterDecorator implements ContentStoreAdapterDecorator {

    public static final Log logger = LogFactory.getLog(TargetedContentStoreAdapterDecorator.class);

    protected ContentStoreAdapter actualStoreAdapter;
    protected CandidateTargetedUrlsResolver candidateTargetedUrlsResolver;

    @Override
    public void setActualStoreAdapter(ContentStoreAdapter actualStoreAdapter) {
        this.actualStoreAdapter = actualStoreAdapter;
    }

    @Required
    public void setCandidateTargetedUrlsResolver(CandidateTargetedUrlsResolver candidateTargetedUrlsResolver) {
        this.candidateTargetedUrlsResolver = candidateTargetedUrlsResolver;
    }

    @Override
    public Context createContext(String id, String rootFolderPath, boolean mergingOn, boolean cacheOn,
                                 int maxAllowedItemsInCache, boolean ignoreHiddenFiles)
            throws StoreException, AuthenticationException {
        Context context = actualStoreAdapter.createContext(id, rootFolderPath, mergingOn, cacheOn,
                                                           maxAllowedItemsInCache, ignoreHiddenFiles);

        return new DecoratedStoreAdapterContext(context, this);
    }

    @Override
    public boolean validate(Context context) throws StoreException, AuthenticationException {
        return actualStoreAdapter.validate(((DecoratedStoreAdapterContext)context).getActualContext());
    }

    @Override
    public void destroyContext(
        Context context) throws InvalidContextException, StoreException, AuthenticationException {
        context = ((DecoratedStoreAdapterContext)context).getActualContext();

        actualStoreAdapter.destroyContext(context);
    }

    @Override
    public boolean exists(Context context, CachingOptions cachingOptions, String path)
        throws InvalidContextException, StoreException {
        context = ((DecoratedStoreAdapterContext)context).getActualContext();

        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(path)) {
            List<String> candidatePaths = candidateTargetedUrlsResolver.getUrls(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    if (actualStoreAdapter.exists(context, cachingOptions, candidatePath)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Targeted of " + path + " found at " + candidatePath);
                        }

                        return true;
                    }
                }

                return false;
            } else {
                return actualStoreAdapter.exists(context, cachingOptions, path);
            }
        } else {
            return actualStoreAdapter.exists(context, cachingOptions, path);
        }
    }

    @Override
    public Content findContent(Context context, CachingOptions cachingOptions,
                               String path) throws InvalidContextException, StoreException {
        context = ((DecoratedStoreAdapterContext)context).getActualContext();

        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(path)) {
            List<String> candidatePaths = candidateTargetedUrlsResolver.getUrls(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    Content content = actualStoreAdapter.findContent(context, cachingOptions, candidatePath);
                    if (content != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Targeted version of " + path + " found at " + candidatePath);
                        }

                        return content;
                    }
                }

                return null;
            } else {
                return actualStoreAdapter.findContent(context, cachingOptions, path);
            }
        } else {
            return actualStoreAdapter.findContent(context, cachingOptions, path);
        }
    }

    @Override
    public Item findItem(Context context, CachingOptions cachingOptions, String path,
                         boolean withDescriptor) throws InvalidContextException, XmlFileParseException, StoreException {
        context = ((DecoratedStoreAdapterContext)context).getActualContext();

        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(path)) {
            List<String> candidatePaths = candidateTargetedUrlsResolver.getUrls(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    Item item = actualStoreAdapter.findItem(context, cachingOptions, candidatePath, withDescriptor);
                    if (item != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Targeted version of " + path + " found at " + candidatePath);
                        }

                        return item;
                    }
                }

                return null;
            } else {
                return actualStoreAdapter.findItem(context, cachingOptions, path, withDescriptor);
            }
        } else {
            return actualStoreAdapter.findItem(context, cachingOptions, path, withDescriptor);
        }
    }

    @Override
    public List<Item> findItems(Context context, CachingOptions cachingOptions, String path)
            throws InvalidContextException, XmlFileParseException,
        StoreException {
        context = ((DecoratedStoreAdapterContext)context).getActualContext();

        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(path)) {
            List<String> candidatePaths = candidateTargetedUrlsResolver.getUrls(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                if (SiteProperties.isMergeFolders()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Merging child items of " + candidatePaths);
                    }

                    List<Item> mergedItems = null;

                    for (String candidatePath : candidatePaths) {
                        List<Item> items = actualStoreAdapter.findItems(context, cachingOptions, candidatePath);
                        mergedItems = mergeItems(mergedItems, items);
                    }

                    return mergedItems;
                } else {
                    for (String candidatePath : candidatePaths) {
                        List<Item> items = actualStoreAdapter.findItems(context, cachingOptions, candidatePath);
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
                return actualStoreAdapter.findItems(context, cachingOptions, path);
            }
        } else {
            return actualStoreAdapter.findItems(context, cachingOptions, path);
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
        int idx = ListUtils.indexOf(list, it -> it.getName().equals(item.getName()));

        return idx >= 0;
    }

}
