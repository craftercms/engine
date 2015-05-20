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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.util.ExceptionUtils;
import org.craftercms.core.util.UrlUtils;
import org.craftercms.engine.exception.HttpStatusCodeAwareException;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.GroovyUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Default controller for rendering Crafter pages. If the site context is the fallback context, a fallback page is
 * always rendered. The controller also tries to find a script controller for the page URL (when not fallback). If
 * one is found, it's executed and its return value is interpreted as a view name to be rendered.
 *
 * @author Alfonso Vasquez
 * @author Dejan Brkic
 */
public class PageRenderController extends AbstractController {
	
	private static final Log logger = LogFactory.getLog(PageRenderController.class);

    private static final String SCRIPT_URL_FORMAT = "%s.%s.%s"; // {url}.{method}.{scriptExt}

    protected String fallbackPageUrl;
    protected ContentStoreService storeService;

    @Required
    public void setFallbackPageUrl(String fallbackPageUrl) {
        this.fallbackPageUrl = fallbackPageUrl;
    }

    @Required
    public void setStoreService(final ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        String pageUrl;
        SiteContext siteContext = SiteContext.getCurrent();

        if (siteContext.isFallback()) {
            logger.warn("Rendering fallback page [" + fallbackPageUrl + "]");

            pageUrl = fallbackPageUrl;
        } else {
            pageUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            if (StringUtils.isEmpty(pageUrl)) {
                throw new IllegalStateException("Required request attribute '" + HandlerMapping
                    .PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
            }

            Script controllerScript = getControllerScript(siteContext, request, pageUrl);
            if (controllerScript != null) {
                Map<String, Object> model = new HashMap<>();
                Map<String, Object> variables = createScriptVariables(request, response, model);
                String viewName = executeScript(controllerScript, variables);

                if (StringUtils.isNotEmpty(viewName)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Rendering view " + viewName + " returned by script " + controllerScript);
                    }

                    return new ModelAndView(viewName, model);
                } else {
                    return null;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Rendering page [" + pageUrl + "]");
            }
        }

        return new ModelAndView(pageUrl);
    }

    protected Script getControllerScript(SiteContext context, HttpServletRequest request, String pageUrl) {
        ScriptFactory scriptFactory = context.getScriptFactory();

        if (scriptFactory == null) {
            throw new IllegalStateException("No script factory associated to current site context '" +
                                            context.getSiteName() + "'");
        }

        String scriptUrl = getScriptUrl(context, scriptFactory, request, pageUrl);

        try {
            // Check controller script exists
            if (storeService.exists(context.getContext(), scriptUrl)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Controller script found for page " + pageUrl + " at " + scriptUrl);
                }

                return scriptFactory.getScript(scriptUrl);
            } else if (logger.isDebugEnabled()) {
                logger.debug("No controller script for page " + pageUrl + " at " + scriptUrl);
            }
        } catch (CrafterException e) {
            logger.error("Error while trying to retrieve controller script at " + scriptUrl, e);
        }

        return null;
    }

    protected String getScriptUrl(SiteContext context, ScriptFactory scriptFactory, HttpServletRequest request,
                                  String pageUrl) {
        String method = request.getMethod().toLowerCase();
        String pageUrlNoExt = FilenameUtils.removeExtension(pageUrl);
        String controllerScriptsPath = context.getControllerScriptsPath();

        String baseUrl = UrlUtils.appendUrl(controllerScriptsPath, pageUrlNoExt);

        return String.format(SCRIPT_URL_FORMAT, baseUrl, method, scriptFactory.getScriptFileExtension());
    }

    protected Map<String, Object> createScriptVariables(HttpServletRequest request, HttpServletResponse response,
                                                        Map<String, Object> model) {
        Map<String, Object> variables = new HashMap<String, Object>();
        GroovyUtils.addCommonVariables(variables, request, response, getServletContext());
        GroovyUtils.addSecurityVariables(variables);
        GroovyUtils.addModelVariable(variables, model);

        return variables;
    }

    protected String executeScript(Script script, Map<String, Object> scriptVariables) throws Exception {
        try {
            Object result = script.execute(scriptVariables);
            if (result != null) {
                if (result instanceof String) {
                    return (String) result;
                } else {
                    throw new ScriptException("Expected String view name as return value of controller script " +
                                              script + ". Actual type of returns value: " +
                                              result.getClass().getName());
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            Exception cause = (Exception) ExceptionUtils.getThrowableOfType(e, HttpStatusCodeAwareException.class);
            if (cause != null) {
                throw cause;
            } else {
                throw e;
            }
        }
    }

}
