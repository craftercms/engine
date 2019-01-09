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
package org.craftercms.engine.controller.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.validation.ValidationException;
import org.craftercms.commons.validation.ValidationRuntimeException;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.util.ExceptionUtils;
import org.craftercms.engine.exception.HttpStatusCodeAwareException;
import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.ScriptUrlTemplateScanner;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.GroovyScriptUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.UriTemplate;

/**
 * Controller for REST script requests.
 *
 * @author Alfonso Vásquez
 */
public class RestScriptsController extends AbstractController {

    private static final Log logger = LogFactory.getLog(RestScriptsController.class);

    public static final String DEFAULT_RESPONSE_BODY_MODEL_ATTR_NAME = "responseBody";
    public static final String DEFAULT_ERROR_MESSAGE_MODEL_ATTR_NAME = "message";

    private static final String SCRIPT_URL_FORMAT = "%s.%s.%s"; // {url}.{method}.{scriptExt}

    protected String responseBodyModelAttributeName;
    protected String errorMessageModelAttributeName;
    protected ScriptUrlTemplateScanner urlTemplateScanner;

    public RestScriptsController() {
        responseBodyModelAttributeName = DEFAULT_RESPONSE_BODY_MODEL_ATTR_NAME;
        errorMessageModelAttributeName = DEFAULT_ERROR_MESSAGE_MODEL_ATTR_NAME;
    }

    public void setResponseBodyModelAttributeName(String responseBodyModelAttributeName) {
        this.responseBodyModelAttributeName = responseBodyModelAttributeName;
    }

    public void setErrorMessageModelAttributeName(String errorMessageModelAttributeName) {
        this.errorMessageModelAttributeName = errorMessageModelAttributeName;
    }

    public void setUrlTemplateScanner(ScriptUrlTemplateScanner urlTemplateScanner) {
        this.urlTemplateScanner = urlTemplateScanner;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        SiteContext siteContext = SiteContext.getCurrent();
        ScriptFactory scriptFactory = siteContext.getScriptFactory();

        if (scriptFactory == null) {
            throw new IllegalStateException(
                "No script factory associate to current site context '" + siteContext.getSiteName() + "'");
        }

        String serviceUrl = getServiceUrl(request);
        String scriptUrl = getScriptUrl(scriptFactory, siteContext, request, serviceUrl);
        Map<String, Object> scriptVariables = createScriptVariables(request, response);

        scriptUrl = parseScriptUrlForVariables(siteContext, scriptUrl, scriptVariables);

        Object responseBody = executeScript(scriptFactory, scriptVariables, response, scriptUrl);

        if (response.isCommitted()) {
            // If response has been already committed by the script, just return null
            logger.debug("Response already committed by script " + scriptUrl);

            return null;
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(responseBodyModelAttributeName, responseBody);

        return modelAndView;
    }

    protected String getServiceUrl(HttpServletRequest request) {
        String pathWithinHandlerMappingAttr = HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;
        String url = (String) request.getAttribute(pathWithinHandlerMappingAttr);

        if (StringUtils.isEmpty(url)) {
            throw new IllegalStateException(
                "Required request attribute '" + pathWithinHandlerMappingAttr + "' is not set");
        }

        return url;
    }

    protected String parseScriptUrlForVariables(SiteContext siteContext, String scriptUrl,
                                                Map<String, Object> variables) {
        ContentStoreService storeService = siteContext.getStoreService();
        if (!storeService.exists(siteContext.getContext(), scriptUrl) && urlTemplateScanner != null) {
            List<UriTemplate> urlTemplates = urlTemplateScanner.scan(siteContext);
            if (CollectionUtils.isNotEmpty(urlTemplates)) {
                for (UriTemplate template : urlTemplates) {
                    if (template.matches(scriptUrl)) {
                        Map<String, String> pathVars = template.match(scriptUrl);
                        String actualScriptUrl = template.toString();

                        variables.put(GroovyScriptUtils.VARIABLE_PATH_VARS, pathVars);

                        return actualScriptUrl;
                    }
                }
            }
        }

        return scriptUrl;
    }

    protected String getScriptUrl(ScriptFactory scriptFactory, SiteContext siteContext, HttpServletRequest request,
                                  String serviceUrl) {
        String baseUrl = UrlUtils.concat(siteContext.getRestScriptsPath(), FilenameUtils.removeExtension(serviceUrl));

        return String.format(SCRIPT_URL_FORMAT, baseUrl, request.getMethod().toLowerCase(), scriptFactory.getScriptFileExtension());
    }

    protected Map<String, Object> createScriptVariables(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> variables = new HashMap<String, Object>();
        GroovyScriptUtils.addRestScriptVariables(variables, request, response, getServletContext());

        return variables;
    }

    protected Object executeScript(ScriptFactory scriptFactory, Map<String, Object> scriptVariables, HttpServletResponse response,
                                   String scriptUrl) {
        try {
            return scriptFactory.getScript(scriptUrl).execute(scriptVariables);
        } catch (ScriptNotFoundException e) {
            logger.error("Script not found at " + scriptUrl, e);

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return Collections.singletonMap(errorMessageModelAttributeName, "REST script not found");
        } catch (Exception e) {
            logger.error("Error executing REST script at " + scriptUrl, e);

            String errorMsg = checkHttpStatusCodeAwareException(e, response);

            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = checkValidationException(e, response);

                if (StringUtils.isEmpty(errorMsg)) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                    errorMsg = e.getMessage();
                }
            }

            return Collections.singletonMap(errorMessageModelAttributeName, errorMsg);
        }
    }

    protected String checkHttpStatusCodeAwareException(Exception e, HttpServletResponse response) {
        HttpStatusCodeAwareException cause = ExceptionUtils.getThrowableOfType(e, HttpStatusCodeAwareException.class);
        if (cause != null) {
            response.setStatus(cause.getStatusCode());

            return ((Exception) cause).getMessage();
        } else {
            return null;
        }
    }

    protected String checkValidationException(Exception e, HttpServletResponse response) {
        Throwable cause = ExceptionUtils.getRootCause(e);
        if (cause instanceof ValidationException || cause instanceof ValidationRuntimeException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            return cause.getMessage();
        } else {
            return null;
        }
    }

}
