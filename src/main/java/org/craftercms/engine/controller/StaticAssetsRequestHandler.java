/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * Request handler to render static assets using the {@link ContentStoreService} as source.
 *
 * @author Alfonso Vásquez
 * @author Jose Ross
 */
public class StaticAssetsRequestHandler extends ResourceHttpRequestHandler {

    private static final Log logger = LogFactory.getLog(StaticAssetsRequestHandler.class);

    private ContentStoreService contentStoreService;
    private String staticAssetsPath;
    private boolean disableCaching;

    protected void init() {
        // Don't require a session for static-assets
        setRequireSession(false);

        // If cache-control is set explicitly, don't change it
        if (getCacheControl() != null) {
            return;
        }

        // If caching is disabled, tell the browser not to cache
        if (disableCaching) {
            setCacheControl(CacheControl.noStore());
        }
    }

    @Required
    public void setContentStoreService(ContentStoreService contentStoreService) {
        this.contentStoreService = contentStoreService;
    }

    public void setStaticAssetsPath(String staticAssetsPath) {
        this.staticAssetsPath = staticAssetsPath;
    }

    public void setDisableCaching(final boolean disableCaching) {
        this.disableCaching = disableCaching;
    }

    @Override
    protected MediaType getMediaType(HttpServletRequest request, Resource resource) {
        MediaType mediaType = super.getMediaType(request, resource);
        MediaType textType = MediaType.parseMediaType("text/*");
        if (textType.includes(mediaType)) {
            return MediaType.parseMediaType(new StringBuilder()
                    .append(mediaType)
                    .append("; charset=")
                    .append(StandardCharsets.UTF_8.name())
                    .toString());
        }

        return mediaType;
    }

    @Override
    protected Resource getResource(final HttpServletRequest request) {

        SiteContext siteContext = SiteContext.getCurrent();
        final String path = getPath(request, siteContext);

        if (siteContext == null) {
            throw new IllegalStateException("No current site context found");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Trying to get content for static asset at [context=" + siteContext + ", path='" + path + "']");
        }

        final Content content = getContent(siteContext, path);
        if (content == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No static asset found at [context=" + siteContext + ", path='" + path +
                    "'] - returning 404");
            }
            return null;
        }

        return toResource(content, path);
    }

    protected Resource toResource(Content content, String path) {
        return new AbstractResource() {

            @Override
            public String getFilename() {
                return FilenameUtils.getName(path);
            }

            @Override
            public long lastModified() {
                // don't provide timestamp to rely on etag comparison instead
                return -1;
            }

            @Override
            public long contentLength() {
                return content.getLength();
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return content.getInputStream();
            }

        };
    }

    protected String getPath(HttpServletRequest request, SiteContext siteContext) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (StringUtils.isEmpty(path)) {
            throw new IllegalStateException("Required request attribute '" + HandlerMapping
                .PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
        }

        if (StringUtils.isNotEmpty(staticAssetsPath)) {
            return UrlUtils.concat(staticAssetsPath, path);
        } else {
            return UrlUtils.concat(siteContext.getStaticAssetsPath(), path);
        }
    }

    protected Content getContent(SiteContext siteContext, String path) {
        return contentStoreService.findContent(siteContext.getContext(), path);
    }



}
