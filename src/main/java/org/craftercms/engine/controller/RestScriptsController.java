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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.util.ExceptionUtils;
import org.craftercms.core.util.UrlUtils;
import org.craftercms.engine.exception.HttpStatusCodeAwareException;
import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.ScriptUtils;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for REST script requests.
 *
 * @author Alfonso Vásquez
 */
public class RestScriptsController extends AbstractController {

    private static final Log logger = LogFactory.getLog(RestScriptsController.class);

    public static final String DEFAULT_RESPONSE_BODY_MODEL_ATTR_NAME = "responseBody";
    public static final String DEFAULT_ERROR_MODEL_ATTR_NAME = "error";

    private static final String SCRIPT_URL_FORMAT = "%s.%s.%s"; // {url}.{method}.{scriptExt}

    protected ScriptFactory scriptFactory;
    protected String responseBodyModelAttributeName;
    protected String errorModelAttributeName;

    public RestScriptsController() {
        responseBodyModelAttributeName = DEFAULT_RESPONSE_BODY_MODEL_ATTR_NAME;
        errorModelAttributeName = DEFAULT_ERROR_MODEL_ATTR_NAME;
    }

    @Required
    public void setScriptFactory(ScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    public void setResponseBodyModelAttributeName(String responseBodyModelAttributeName) {
        this.responseBodyModelAttributeName = responseBodyModelAttributeName;
    }

    public void setErrorModelAttributeName(String errorModelAttributeName) {
        this.errorModelAttributeName = errorModelAttributeName;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SiteContext siteContext = AbstractSiteContextResolvingFilter.getCurrentContext();
        String serviceUrl = getServiceUrl(request);
        String scriptUrl = getScriptUrl(siteContext, request, serviceUrl);

        Object responseBody = executeScript(request, response, scriptUrl);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(responseBodyModelAttributeName, responseBody);

        return modelAndView;
    }

    protected String getServiceUrl(HttpServletRequest request) {
        String pathWithinHandlerMappingAttr = HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;
        String url = (String) request.getAttribute(pathWithinHandlerMappingAttr);

        if (StringUtils.isEmpty(url)) {
            throw new IllegalStateException("Required request attribute '" + pathWithinHandlerMappingAttr + "' is not set");
        }

        return url;
    }

    protected String getScriptUrl(SiteContext siteContext, HttpServletRequest request, String serviceUrl) {
        String baseUrl = UrlUtils.appendUrl(siteContext.getRestScriptsPath(), FilenameUtils.removeExtension(serviceUrl));

        return String.format(SCRIPT_URL_FORMAT, baseUrl, request.getMethod().toLowerCase(), scriptFactory.getScriptFileExtension());
    }

    protected Map<String, Object> createScriptVariables(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> scriptVariables = new HashMap<String, Object>();
        ScriptUtils.addCommonVariables(scriptVariables, request, response, getServletContext());
        ScriptUtils.addCrafterVariables(scriptVariables);

        return scriptVariables;
    }

    protected Object executeScript(HttpServletRequest request, HttpServletResponse response, String scriptUrl) {
        Map<String, Object> scriptVariables = createScriptVariables(request, response);

        try {
            return scriptFactory.getScript(scriptUrl).execute(scriptVariables);
        } catch (ScriptNotFoundException e) {
            logger.error("Script not found at " + scriptUrl, e);

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return Collections.singletonMap(errorModelAttributeName, "REST script not found");
        } catch (Exception e) {
            logger.error("Execution failed for script " + scriptUrl, e);

            HttpStatusCodeAwareException httpStatusAwareEx = ExceptionUtils.getThrowableOfType(e, HttpStatusCodeAwareException.class);
            String errorMsg;

            if (httpStatusAwareEx != null) {
                response.setStatus(httpStatusAwareEx.getStatusCode());

                errorMsg = ((Exception) httpStatusAwareEx).getMessage();
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                errorMsg = e.getMessage();
            }

            return Collections.singletonMap(errorModelAttributeName, errorMsg);
        }
    }

}
