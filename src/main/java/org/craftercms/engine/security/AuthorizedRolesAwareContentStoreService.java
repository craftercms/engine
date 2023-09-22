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
import org.craftercms.core.service.impl.ContentStoreServiceImpl;
import org.craftercms.core.store.ContentStoreAdapterRegistry;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategyResolver;
import org.craftercms.core.xml.mergers.DescriptorMerger;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.io.File;
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
    public Item findItem(Context context, CachingOptions cachingOptions, String url, ItemProcessor processor, boolean flatten) throws InvalidContextException, XmlFileParseException, XmlMergeException, ItemProcessingException, StoreException {
        Item item = super.findItem(context, cachingOptions, url, processor, flatten);
        if (item == null) {
            return null;
        }
        checkAccess(item, context, cachingOptions, processor, flatten);
        return item;
    }

    @Override
    protected List<Item> getChildrenInternal(Context context, CachingOptions cachingOptions, String url, ItemProcessor processor, boolean flatten) {
        List<Item> children = super.getChildrenInternal(context, cachingOptions, url, processor, flatten);
        if (children == null) {
            return null;
        }
        return children.stream()
                .filter(item -> {
                    try {
                        checkAccess(item, context, cachingOptions, processor, flatten);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    protected void checkAccess(Item item, Context context, CachingOptions cachingOptions, ItemProcessor processor, boolean flatten) {
        Item accessHolderItem = item;
        if (item.isFolder()) {
            // For folders, check if there is an /index.xml file and use that item to check access
            Item defaultPageItem = super.findItem(context, cachingOptions, item.getUrl() + File.separator + SiteProperties.DEFAULT_INDEX_FILE_NAME, processor, flatten);
            if (defaultPageItem != null) {
                accessHolderItem = defaultPageItem;
            }
        }
        try {
            List<String> roles = accessHolderItem.queryDescriptorValues(authorizedRolesXPathQuery);
            SecurityUtils.checkAccess(roles, accessHolderItem.getUrl());
        } catch (AccessDeniedException e) {
            // User is authenticated but does not have access to the item
            logger.debug("Access denied for item: '{}': '{}'", item.getUrl(), e.getMessage());
            throw new ForbiddenPathException(e.getMessage());
        } catch (AuthenticationException e) {
            // User is anonymous and item requires authentication
            logger.debug("Authentication failed for item: '{}': '{}'", item.getUrl(), e.getMessage());
            throw new org.craftercms.core.exception.AuthenticationException(e.getMessage(), e);
        }
    }
}
