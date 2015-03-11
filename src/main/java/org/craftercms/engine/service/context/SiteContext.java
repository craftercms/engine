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

import javax.servlet.http.HttpServletRequest;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.service.PreviewOverlayCallback;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

/**
 * Wrapper for a {@link Context} that adds properties specific to Crafter Engine.
 *
 * @author Alfonso Vásquez
 */
public class SiteContext {

    public static final String SITE_CONTEXT_ATTRIBUTE = "siteContext";
    public static final String SITE_NAME_ATTRIBUTE = "siteName";

    protected ContentStoreService storeService;
    protected String siteName;
    protected Context context;
    protected boolean fallback;
    protected String staticAssetsPath;
    protected String templatesPath;
    protected FreeMarkerConfig freeMarkerConfig;
    protected String restScriptsPath;
    protected String controllerScriptsPath;
    protected UrlTransformationEngine urlTransformationEngine;
    protected PreviewOverlayCallback overlayCallback;

    public static SiteContext getCurrent() {
        return (SiteContext) RequestContext.getCurrent().getRequest().getAttribute(SITE_CONTEXT_ATTRIBUTE);
    }

    public static void setCurrent(SiteContext context) {
        HttpServletRequest request = RequestContext.getCurrent().getRequest();

        request.setAttribute(SITE_CONTEXT_ATTRIBUTE, context);
        request.setAttribute(SITE_NAME_ATTRIBUTE, context.getSiteName());
    }

    public SiteContext(ContentStoreService storeService, String siteName, Context context, boolean fallback,
                       String staticAssetsPath, String templatesPath, FreeMarkerConfig freeMarkerConfig,
                       String restScriptsPath, String controllerScriptsPath,
                       UrlTransformationEngine urlTransformationEngine, PreviewOverlayCallback overlayCallback) {
        this.storeService = storeService;
        this.siteName = siteName;
        this.context = context;
        this.fallback = fallback;
        this.staticAssetsPath = staticAssetsPath;
        this.templatesPath = templatesPath;
        this.freeMarkerConfig = freeMarkerConfig;
        this.restScriptsPath = restScriptsPath;
        this.controllerScriptsPath = controllerScriptsPath;
        this.urlTransformationEngine = urlTransformationEngine;
        this.overlayCallback = overlayCallback;
    }

    public String getSiteName() {
        return siteName;
    }

    public Context getContext() {
        return context;
    }

    public boolean isFallback() {
        return fallback;
    }

    public String getStaticAssetsPath() {
        return staticAssetsPath;
    }

    public String getTemplatesPath() {
        return templatesPath;
    }

    public FreeMarkerConfig getFreeMarkerConfig() {
        return freeMarkerConfig;
    }

    public String getRestScriptsPath() {
        return restScriptsPath;
    }

    public String getControllerScriptsPath() {
        return controllerScriptsPath;
    }

    public UrlTransformationEngine getUrlTransformationEngine() {
        return urlTransformationEngine;
    }

    public PreviewOverlayCallback getOverlayCallback() {
        return overlayCallback;
    }

    public void destroy() throws CrafterException {
        storeService.destroyContext(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SiteContext context = (SiteContext) o;

        if (!siteName.equals(context.siteName)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return siteName.hashCode();
    }

    @Override
    public String toString() {
        return "SiteContext[" +
                "siteName='" + siteName + '\'' +
                ", context=" + context +
                ", fallback=" + fallback +
                ", staticAssetsPath='" + staticAssetsPath + '\'' +
                ", templatesPath='" + templatesPath + '\'' +
                ", freeMarkerConfig=" + freeMarkerConfig +
                ", restScriptsPath='" + restScriptsPath + '\'' +
                ", controllerScriptsPath='" + controllerScriptsPath + '\'' +
                ", urlTransformationEngine=" + urlTransformationEngine +
                ", overlayCallback=" + overlayCallback +
                ']';
    }

}
