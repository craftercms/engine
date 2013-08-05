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

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.filesystem.FileSystemContentStoreAdapter;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.macro.MacroResolver;
import org.craftercms.engine.service.PreviewOverlayCallback;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

/**
 * Factory for creating {@link SiteContext} with common properties. It also uses the {@link MacroResolver} to resolve any macros
 * specified in the {@code rootFolderPath} before creating the context (remember that macros can vary between requests).
 *
 * @author Alfonso Vásquez
 */
public class SiteContextFactory {

    protected String storeType;
    protected String storeServerUrl;
    protected String username;
    protected String password;
    protected String rootFolderPath;
    protected String staticAssetsPath;
    protected String templatesPath;
    protected ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory;
    protected String restScriptsPath;
    protected String restScriptTemplatesPath;
    protected ObjectFactory<FreeMarkerConfig> restScriptsFreeMarkerConfigFactory;
    protected boolean cacheOn;
    protected int maxAllowedItemsInCache;
    protected boolean ignoreHiddenFiles;
    protected UrlTransformationEngine urlTransformationEngine;
    protected PreviewOverlayCallback overlayCallback;
    protected ContentStoreService storeService;
    protected MacroResolver macroResolver;

    public SiteContextFactory() {
        storeType = FileSystemContentStoreAdapter.STORE_TYPE;
        storeServerUrl = null;
        username = null;
        password = null;
        cacheOn = Context.DEFAULT_CACHE_ON;
        maxAllowedItemsInCache = Context.DEFAULT_MAX_ALLOWED_ITEMS_IN_CACHE;
        ignoreHiddenFiles = Context.DEFAULT_IGNORE_HIDDEN_FILES;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public void setStoreServerUrl(String storeServerUrl) {
        this.storeServerUrl = storeServerUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Required
    public void setRootFolderPath(String rootFolderPath) {
        this.rootFolderPath = rootFolderPath;
    }

    @Required
    public void setStaticAssetsPath(String staticAssetsPath) {
        this.staticAssetsPath = staticAssetsPath;
    }

    @Required
    public void setTemplatesPath(String templatesPath) {
        this.templatesPath = templatesPath;
    }

    @Required
    public void setFreeMarkerConfigFactory(ObjectFactory<FreeMarkerConfig> freeMarkerConfigFactory) {
        this.freeMarkerConfigFactory = freeMarkerConfigFactory;
    }

    @Required
    public void setRestScriptsPath(String restScriptsPath) {
        this.restScriptsPath = restScriptsPath;
    }

    @Required
    public void setRestScriptTemplatesPath(String restScriptTemplatesPath) {
        this.restScriptTemplatesPath = restScriptTemplatesPath;
    }

    @Required
    public void setRestScriptsFreeMarkerConfigFactory(ObjectFactory<FreeMarkerConfig> restScriptsFreeMarkerConfigFactory) {
        this.restScriptsFreeMarkerConfigFactory = restScriptsFreeMarkerConfigFactory;
    }

    public void setCacheOn(boolean cacheOn) {
        this.cacheOn = cacheOn;
    }

    public void setMaxAllowedItemsInCache(int maxAllowedItemsInCache) {
        this.maxAllowedItemsInCache = maxAllowedItemsInCache;
    }

    public void setIgnoreHiddenFiles(boolean ignoreHiddenFiles) {
        this.ignoreHiddenFiles = ignoreHiddenFiles;
    }

    @Required
    public void setUrlTransformationEngine(UrlTransformationEngine urlTransformationEngine) {
        this.urlTransformationEngine = urlTransformationEngine;
    }

    public void setOverlayCallback(PreviewOverlayCallback overlayCallback) {
        this.overlayCallback = overlayCallback;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Required
    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    public SiteContext createContext(String siteName, boolean fallback) {
        String resolvedRootFolderPath = macroResolver.resolveMacros(rootFolderPath);

        Context context = storeService.createContext(storeType, storeServerUrl, username, password, resolvedRootFolderPath, cacheOn,
                maxAllowedItemsInCache, ignoreHiddenFiles);

        return new SiteContext(siteName, context, fallback, staticAssetsPath, templatesPath, getFreemarkerConfig(), restScriptsPath,
                restScriptTemplatesPath, getRestScriptsFreeMarkerConfig(), urlTransformationEngine, overlayCallback);
    }

    protected FreeMarkerConfig getFreemarkerConfig() {
        return freeMarkerConfigFactory.getObject();
    }

    protected FreeMarkerConfig getRestScriptsFreeMarkerConfig() {
        return restScriptsFreeMarkerConfigFactory.getObject();
    }

}
