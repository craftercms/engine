/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.security;

import org.craftercms.commons.file.blob.BlobStoreResolver;
import org.craftercms.commons.file.blob.BlobUrlResolver;
import org.craftercms.core.exception.*;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.processors.ItemProcessorResolver;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;
import org.craftercms.core.service.impl.ContentStoreServiceImpl;
import org.craftercms.core.store.ContentStoreAdapterRegistry;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategyResolver;
import org.craftercms.core.xml.mergers.DescriptorMerger;
import org.craftercms.engine.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Extends {@link ContentStoreServiceImpl} to add checks for authorizedRoles property if present in content.
 */
public class AuthorizedRolesAwareContentStoreService extends ContentStoreServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizedRolesAwareContentStoreService.class);

    // Meant to support any root element name
    private static final String ROOT_ELEMENT_XPATH_PREFIX = "*/";
    private final String authorizedRolesXPathQuery;

    public AuthorizedRolesAwareContentStoreService(CacheTemplate cacheTemplate, ContentStoreAdapterRegistry storeAdapterRegistry,
                                                   DescriptorMergeStrategyResolver mergeStrategyResolver,
                                                   DescriptorMerger merger, ItemProcessorResolver processorResolver,
                                                   BlobUrlResolver blobUrlResolver, BlobStoreResolver blobStoreResolver,
                                                   String sourceAttributeName, String sourceTypeAttributeName,
                                                   String sourceTypeXPath, String authorizedRolesXPathQuery) {
        super(cacheTemplate, storeAdapterRegistry, mergeStrategyResolver, merger, processorResolver, blobUrlResolver, blobStoreResolver, sourceAttributeName, sourceTypeAttributeName, sourceTypeXPath);
        this.authorizedRolesXPathQuery = ROOT_ELEMENT_XPATH_PREFIX + authorizedRolesXPathQuery;
    }

    @Override
    public Item getItem(Context context, CachingOptions cachingOptions, String url, ItemProcessor processor, boolean flatten) throws InvalidContextException, PathNotFoundException, XmlFileParseException, XmlMergeException, ItemProcessingException, StoreException {
        Item item = super.getItem(context, cachingOptions, url, processor, flatten);
        checkAccess(url, item);
        return item;
    }

    private void checkAccess(String url, Item item) {
        try {
            List<String> roles = item.queryDescriptorValues(authorizedRolesXPathQuery);
            SecurityUtils.checkAccess(roles, url);
        } catch (AccessDeniedException e) {
            logger.debug("Access denied for item: '{}': '{}'", item.getUrl(), e.getMessage());
            throw new StoreAccessDeniedException(e.getMessage(), e);
        }
    }

    @Override
    protected List<Item> doFilter(List<Item> items, ItemFilter filter, boolean runningBeforeProcessing) {
        List<Item> filteredItems = super.doFilter(items, filter, runningBeforeProcessing);

        if (!runningBeforeProcessing) {
            filteredItems = filteredItems.stream()
                    .filter(item -> {
                        try {
                            checkAccess(item.getUrl(), item);
                            return true;
                        } catch (AccessDeniedException e) {
                            logger.warn("Access denied for item: '{}'", item.getUrl(), e);
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        return filteredItems;
    }
}
