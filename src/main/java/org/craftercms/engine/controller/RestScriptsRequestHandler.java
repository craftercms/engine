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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.Tika;
import org.craftercms.core.util.UrlUtils;
import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.exception.ScriptViewNotFoundException;
import org.craftercms.engine.scripting.*;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Request handler for REST scripts.
 *
 * @author Alfonso Vásquez
 */
public class RestScriptsRequestHandler extends WebApplicationObjectSupport implements HttpRequestHandler {

    private static final Log logger = LogFactory.getLog(RestScriptsRequestHandler.class);

    private static final String SCRIPT_URL_FORMAT = "%s.%s.%s"; // {url}.{method}.{scriptExt}

    private static final String FORMAT_REQUEST_PARAM = "format";

    private static final MediaType DEFAULT_MIME_TYPE = MediaType.APPLICATION_JSON;

    protected ScriptFactory scriptFactory;
    protected ScriptViewResolver scriptViewResolver;
    protected Tika tika;

    public RestScriptsRequestHandler() {
        tika = new Tika();
    }

    @Required
    public void setScriptFactory(ScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Required
    public void setScriptViewResolver(ScriptViewResolver scriptViewResolver) {
        this.scriptViewResolver = scriptViewResolver;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SiteContext siteContext = AbstractSiteContextResolvingFilter.getCurrentContext();

        String serviceUrl = getServiceUrl(request);
        String scriptUrl = getScriptUrl(siteContext, request, serviceUrl);
        List<MediaType> acceptableMimeTypes = getAcceptableMimeTypes(request, serviceUrl);
        String viewName = getViewName(serviceUrl);
        Status status = new Status();
        Map<String, Object> model = new HashMap<String, Object>();

        executeScript(request, response, scriptUrl, status, model);
        renderView(request, response, viewName, acceptableMimeTypes, status, model);
    }

    protected String getServiceUrl(HttpServletRequest request) {
        String url = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (StringUtils.isEmpty(url)) {
            throw new IllegalStateException("Required request attribute '" + HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE +
                    "' is not set");
        }

        return url;
    }

    protected String getScriptUrl(SiteContext siteContext, HttpServletRequest request, String serviceUrl) {
        String baseUrl = UrlUtils.appendUrl(siteContext.getRestScriptsPath(), FilenameUtils.removeExtension(serviceUrl));

        return String.format(SCRIPT_URL_FORMAT, baseUrl, request.getMethod().toLowerCase(), scriptFactory.getScriptFileExtension());
    }

    protected String getViewName(String serviceUrl) {
        return FilenameUtils.removeExtension(serviceUrl);
    }

    protected List<MediaType> getAcceptableMimeTypes(HttpServletRequest request, String serviceUrl) {
        List<MediaType> acceptableMediaTypes = null;

        // Look first for the format in the extension
        String format = FilenameUtils.getExtension(serviceUrl);
        if (StringUtils.isNotEmpty(format)) {
            MediaType mimeType = getMimeType(format);
            if (mimeType != null) {
                acceptableMediaTypes = Collections.singletonList(mimeType);
            }
        }

        if (CollectionUtils.isEmpty(acceptableMediaTypes)) {
            // Try looking for the format in a param
            format = request.getParameter(FORMAT_REQUEST_PARAM);
            if (StringUtils.isNotEmpty(format)) {
                MediaType mimeType = getMimeType(format);
                if (mimeType != null) {
                    acceptableMediaTypes = Collections.singletonList(mimeType);
                }
            }
        }

        if (CollectionUtils.isEmpty(acceptableMediaTypes)) {
            // Use the mime types from the Accept header
            String acceptHeader = request.getHeader("Accept");
            if (StringUtils.isNotEmpty(acceptHeader)) {
                acceptableMediaTypes = MediaType.parseMediaTypes(acceptHeader);
            }
        }

        if (CollectionUtils.isEmpty(acceptableMediaTypes)) {
            // Use default mime type
            acceptableMediaTypes = Collections.singletonList(DEFAULT_MIME_TYPE);
        }

        return acceptableMediaTypes;
    }

    protected MediaType getMimeType(String format) {
        String type = tika.detect("format." + format);
        if (StringUtils.isNotEmpty(type)) {
            return MediaType.valueOf(type);
        } else {
            return null;
        }
    }

    protected Map<String, Object> createScriptVariables(HttpServletRequest request, HttpServletResponse response, Status status,
                                                        Map<String, Object> model) {
        Map<String, Object> scriptVariables = ScriptUtils.createServletVariables(request, response, getServletContext());
        scriptVariables.put("model", model);
        scriptVariables.put("status", status);
        scriptVariables.put("logger", logger);

        return scriptVariables;
    }

    protected void executeScript(HttpServletRequest request, HttpServletResponse response, String scriptUrl, Status status,
                                 Map<String, Object> model) {
        Map<String, Object> scriptVariables = createScriptVariables(request, response, status, model);

        try {
            scriptFactory.getScript(scriptUrl).execute(scriptVariables);
        } catch (ScriptNotFoundException e) {
            logger.error("Script " + scriptUrl + " not found", e);

            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setRedirect(true);
            model.put("message", e.getMessage());
            model.put("exception", e);
        } catch (Exception e) {
            logger.error("Execution failed for script " + scriptUrl, e);

            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setRedirect(true);
            model.put("message", e.getMessage());
            model.put("exception", e);
        }
    }

    protected void renderView(HttpServletRequest request, HttpServletResponse response, String viewName,
                              List<MediaType> acceptableMimeTypes, Status status, Map<String, Object> model) {
        Locale locale = RequestContextUtils.getLocale(request);
        String method = request.getMethod().toLowerCase();
        ScriptView view = scriptViewResolver.resolveView(viewName, method, acceptableMimeTypes, status, locale);

        response.setStatus(status.getCode());

        if (view != null) {
            view.render(status, model, request, response);
        } else {
            throw new ScriptViewNotFoundException("No script view found for '" + viewName + "'");
        }
    }

}
