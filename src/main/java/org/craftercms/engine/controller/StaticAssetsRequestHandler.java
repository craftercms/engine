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
package org.craftercms.engine.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.util.UrlUtils;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * Request handler to render static assets. Similar to {@link org.springframework.web.servlet.resource.ResourceHttpRequestHandler}, but
 * instead of using Spring Resources, it uses the {@link ContentStoreService#getContent(Context, String)}.
 *
 * @author Alfonso Vásquez
 */
public class StaticAssetsRequestHandler extends WebContentGenerator implements HttpRequestHandler {

    private static final Log logger = LogFactory.getLog(StaticAssetsRequestHandler.class);

    private ContentStoreService contentStoreService;
    private String staticAssetsPath;
    private boolean disableCaching;

    public StaticAssetsRequestHandler() {
        super(METHOD_GET, METHOD_HEAD);

        setRequireSession(false);
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
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        checkAndPrepare(request, response, true);

        SiteContext context = SiteContext.getCurrent();
        String path = getPath(request, context);

        if (context == null) {
            throw new IllegalStateException("No current site context found");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Trying to get content for static asset at [context=" + context + ", path='" + path + "']");
        }

        Content content = getContent(context, path);
        if (content == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No static asset found at [context=" + context + ", path='" + path + "'] - returning 404");
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        MediaType mediaType = getMediaType(path);
        if (mediaType != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Determined media type '" + mediaType + "' for static asset at [context=" + context +
                    ", path='" + path + "']");
            }
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("No media type found for static asset at [context=" + context + ", path='" + path +
                    "'] - not sending a content-type header");
            }
        }

        if ((new ServletWebRequest(request, response)).checkNotModified(content.getLastModified())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Static asset not modified - returning 304");
            }

            return;
        }

        setHeaders(response, content, mediaType);

        if (disableCaching) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caching disabled on client");
            }

            HttpUtils.disableCaching(response);
        }

        if (METHOD_HEAD.equals(request.getMethod())) {
            logger.trace("HEAD request - skipping content");

            return;
        }

        writeContent(response, content);
    }

    protected String getPath(HttpServletRequest request, SiteContext context) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (StringUtils.isEmpty(path)) {
            throw new IllegalStateException("Required request attribute '" + HandlerMapping
                .PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
        }

        if (StringUtils.isNotEmpty(staticAssetsPath)) {
            return UrlUtils.appendUrl(staticAssetsPath, path);
        } else {
            return UrlUtils.appendUrl(context.getStaticAssetsPath(), path);
        }
    }

    protected Content getContent(SiteContext context, String path) {
        try {
            return contentStoreService.getContent(context.getContext(), path);
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    protected MediaType getMediaType(String path) {
        String mimeType = getServletContext().getMimeType(FilenameUtils.getName(path));

        return (StringUtils.isNotEmpty(mimeType)? MediaType.parseMediaType(mimeType) : null);
    }

    protected void setHeaders(HttpServletResponse response, Content content, MediaType mediaType) throws IOException {
        response.setContentLength((int) content.getLength());
        if (mediaType != null) {
            response.setContentType(mediaType.toString());
        }
    }

    protected void writeContent(HttpServletResponse response, Content content) throws IOException {
        FileCopyUtils.copy(content.getInputStream(), response.getOutputStream());
    }

}
