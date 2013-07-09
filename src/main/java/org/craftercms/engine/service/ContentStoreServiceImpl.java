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
package org.craftercms.engine.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.craftercms.core.exception.*;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.service.*;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategy;

import java.util.*;

import org.craftercms.core.util.HttpServletUtils;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.macro.MacroResolver;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;

/**
 * Override {@link org.craftercms.core.service.impl.ContentStoreServiceImpl} with
 * fallback site logic. For example, the fallback site of 'test-fr' is 'test'.
 *
 * @author Russ Danner, Michael Chen
 */
public class ContentStoreServiceImpl extends org.craftercms.core.service.impl.ContentStoreServiceImpl
{
    private MacroResolver macroResolver;
    private String sitePathPrefix;

    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    public void setSitePathPrefix(String sitePathPrefix) {
        this.sitePathPrefix = sitePathPrefix;
    }

    /**
     * Returns the content store item for the given url.
     *
     * <p>After acquiring the item from the {@link ContentStoreAdapter}, the item's descriptor is merged (according to its
     * {@link DescriptorMergeStrategy}) with related descriptors, and the final item is then processed.</p>
     */
    @Override
    protected Item doGetItem(Context context, CachingOptions cachingOptions, String url, ItemProcessor processor)
            throws InvalidContextException, PathNotFoundException, XmlFileParseException,
                   XmlMergeException, ItemProcessingException, StoreException
    {
        Item retItem = null;
        String currentSite = getSiteName();
        String siteFolders = macroResolver.resolveMacros(sitePathPrefix);

        while (currentSite.length() > 0) {
            try {
                String expandedUrl =  (url.startsWith(siteFolders)) ? url : siteFolders + url;
                retItem = super.doGetItem(context, cachingOptions, expandedUrl, processor);

                retItem.setUrl(retItem.getUrl().replace(siteFolders, ""));
                retItem.setDescriptorUrl(retItem.getDescriptorUrl().replace(siteFolders, ""));
                break;
            }
            catch (Exception PathNotFoundException) {
                // it's ok, fallback
            }
            // calulate the next base value
            int dash = currentSite.lastIndexOf('-');
            String baseSite = currentSite.substring(0, dash > 0 ? dash : 0);
            siteFolders = siteFolders.replaceAll(currentSite, baseSite);
            currentSite = baseSite;
        }

        if (retItem == null) {
            // ok we really can't find it.
            throw new PathNotFoundException(url);
        }

        return retItem;
    }
    
    @Override
    public Content getContent(Context context, CachingOptions cachingOptions, String url)
        throws InvalidScopeException, PathNotFoundException, StoreException
    {
        Content retItem = null;
        String currentSite = getSiteName();
        String siteFolders = macroResolver.resolveMacros(sitePathPrefix);

        while (currentSite.length() > 0) {
            try {
                String expandedUrl =  (url.startsWith(siteFolders)) ? url : siteFolders + url;
                retItem = super.getContent(context, cachingOptions, expandedUrl);
                break;
            }
            catch (Exception PathNotFoundException) {
                // it's ok, fallback
            }
            // calulate the next base value
            int dash = currentSite.lastIndexOf('-');
            String baseSite = currentSite.substring(0, dash > 0 ? dash : 0);
            siteFolders = siteFolders.replaceAll(currentSite, baseSite);
            currentSite = baseSite;
        }

        if (retItem == null) {
            // ok we really can't find it.
            throw new PathNotFoundException(url);
        }

        return retItem;
    }


    /**
     * Does the following:
     * 
     * <ol>
     *     <li>Retrieves the children from the underlying repository (without their descriptors).</li>
     *     <li>Filters the returned list if {@link ItemFilter#runBeforeProcessing()} returns <code>true</code>.</li>
     *     <li>Calls {@link #getTree(Context, String)} or {@link #getItem(Context, String)} for each item in the list (depending on
     *     whether the item is a folder or not, and if <code>depth</code> is not null), to obtain the merged and processed version
     *     of each item.</li>
     *     <li>Filters the processed list if {@link ItemFilter#runAfterProcessing()} ()} returns <code>true</code>.</li>
     *     <li>Returns the final list of processed items.</li>
     * </ol>
     */
    protected List<Item> doGetChildren(Context context, CachingOptions cachingOptions, String url, Integer depth, ItemFilter filter,
                                       ItemProcessor processor) throws InvalidContextException, PathNotFoundException,
            XmlFileParseException, XmlMergeException, ItemProcessingException, StoreException
    {
        List<Item> retItems = null;
        Map<String, Item> itemMap = new HashMap<String, Item>();
        String currentSite = getSiteName();
        String siteFolders = macroResolver.resolveMacros(sitePathPrefix);

        while (currentSite.length() > 0) {
            try {
                String expandedUrl =  (url.startsWith(siteFolders)) ? url : siteFolders + url;
                List<Item> items = super.doGetChildren(context, cachingOptions, expandedUrl, depth, filter, processor);

                retItems = items;

                for (Item listItem : items) {
                    String key = listItem.getUrl().replace(siteFolders, "");
                    itemMap.put(key, listItem);
                }
            }
            catch (Exception PathNotFoundException) {
                // it's ok, fallback
            }
            // calulate the next base value
            int dash = currentSite.lastIndexOf('-');
            String baseSite = currentSite.substring(0, dash > 0 ? dash : 0);
            siteFolders = siteFolders.replaceAll(currentSite, baseSite);
            currentSite = baseSite;
        }

        if (retItems == null) {
            // We got nothing
            return new CachingAwareList<Item>();
        }

        retItems.clear();
        for(Item mapItem : itemMap.values()) {
            retItems.add(mapItem);
        }
        return retItems;
    }

    protected String createContextId(String storeType, String storeServerUrl, String username, String password, String rootFolderPath,
            boolean cacheOn, int maxAllowedItemsInCache, boolean ignoreHiddenFiles) {
        String unHashedId = "storeType='" + storeType + '\'' +
            ", storeServerUrl='" + storeServerUrl + '\'' +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", rootFolderPath='" + rootFolderPath + '\'' +
            ", cacheOn=" + cacheOn +
            ", maxAllowedItemsInCache=" + maxAllowedItemsInCache +
            ", ignoreHiddenFiles=" + ignoreHiddenFiles +
            ", previewsite=" + getSiteName();

        return DigestUtils.md5Hex(unHashedId);
    }

    private static final String noSiteName =
        "No '"+ AbstractSiteContextResolvingFilter.SITE_NAME_ATTRIBUTE+"' specified in request.";

    private String getSiteName() {
        Object siteName = HttpServletUtils.getAttribute(AbstractSiteContextResolvingFilter.SITE_NAME_ATTRIBUTE, HttpServletUtils.SCOPE_REQUEST);
        if (siteName != null) {
            return siteName.toString();
        }
        throw new CrafterException(noSiteName);
    }
}
