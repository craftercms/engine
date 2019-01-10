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
package org.craftercms.engine.util;

import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.impl.GroovyScript;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.spring.ApplicationContextAccessor;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Utility methods for Groovy scripts.
 *
 * @author Alfonso Vásquez
 */
public class GroovyScriptUtils {

    public static final Logger GROOVY_SCRIPT_LOGGER = LoggerFactory.getLogger(GroovyScript.class);

    public static final String VARIABLE_APPLICATION = "application";
    public static final String VARIABLE_REQUEST = "request";
    public static final String VARIABLE_RESPONSE = "response";
    public static final String VARIABLE_PARAMS = "params";
    public static final String VARIABLE_PATH_VARS = "pathVars";
    public static final String VARIABLE_HEADERS = "headers";
    public static final String VARIABLE_COOKIES = "cookies";
    public static final String VARIABLE_SESSION = "session";
    public static final String VARIABLE_LOGGER = "logger";
    public static final String VARIABLE_LOCALE = "locale";
    @Deprecated
    public static final String VARIABLE_MODEL = "model";
    public static final String VARIABLE_TEMPLATE_MODEL = "templateModel";
    @Deprecated
    public static final String VARIABLE_CRAFTER_MODEL = "crafterModel";
    public static final String VARIABLE_CONTENT_MODEL = "contentModel";
    public static final String VARIABLE_AUTH = "authentication";
    public static final String VARIABLE_PROFILE = "profile";
    public static final String VARIABLE_SITE_CONTEXT = "siteContext";
    public static final String VARIABLE_SITE_CONFIG = "siteConfig";
    public static final String VARIABLE_FILTER_CHAIN = "filterChain";
    public static final String VARIABLE_APPLICATION_CONTEXT = "applicationContext";

    private GroovyScriptUtils() {
    }

    public static void addRestScriptVariables(Map<String, Object> variables, HttpServletRequest request,
                                              HttpServletResponse response, ServletContext servletContext) {
        addCommonVariables(variables, request, response, servletContext);
        addSecurityVariables(variables);
    }

    public static void addSiteItemScriptVariables(Map<String, Object> variables, HttpServletRequest request,
                                                  HttpServletResponse response, ServletContext servletContext,
                                                  SiteItem item, Object templateModel) {
        addCommonVariables(variables, request, response, servletContext);
        addSecurityVariables(variables);
        addContentModelVariable(variables, item);
        addTemplateModelVariable(variables, templateModel);
    }


    public static void addControllerScriptVariables(Map<String, Object> variables, HttpServletRequest request,
                                                    HttpServletResponse response, ServletContext servletContext,
                                                    Object templateModel) {
        addCommonVariables(variables, request, response, servletContext);
        addSecurityVariables(variables);
        addTemplateModelVariable(variables, templateModel);
    }

    public static void addFilterScriptVariables(Map<String, Object> variables, HttpServletRequest request,
                                                HttpServletResponse response, ServletContext servletContext,
                                                FilterChain filterChain) {
        addCommonVariables(variables, request, response, servletContext);
        addSecurityVariables(variables);
        addFilterChainVariable(variables, filterChain);
    }

    public static void addJobScriptVariables(Map<String, Object> variables, ServletContext servletContext) {
        SiteContext siteContext = SiteContext.getCurrent();

        if (siteContext != null && siteContext.getApplicationContext() != null) {
            ApplicationContextAccessor appContext = new ApplicationContextAccessor();
            appContext.setApplicationContext(siteContext.getApplicationContext());

            variables.put(VARIABLE_APPLICATION_CONTEXT, appContext);
        }

        variables.put(VARIABLE_APPLICATION, servletContext);
        variables.put(VARIABLE_LOGGER, GROOVY_SCRIPT_LOGGER);

        addSiteContextVariable(variables);
        addSiteConfigVariable(variables);
    }

    private static void addCommonVariables(Map<String, Object> variables, HttpServletRequest request,
                                           HttpServletResponse response, ServletContext servletContext) {
        variables.put(VARIABLE_APPLICATION, servletContext);
        variables.put(VARIABLE_REQUEST, request);
        variables.put(VARIABLE_RESPONSE, response);

        if (request != null) {
            variables.put(VARIABLE_PARAMS, HttpUtils.createRequestParamsMap(request));
            variables.put(VARIABLE_HEADERS, HttpUtils.createHeadersMap(request));
            variables.put(VARIABLE_COOKIES, HttpUtils.createCookiesMap(request));
            variables.put(VARIABLE_SESSION, request.getSession(false));
        } else {
            variables.put(VARIABLE_PARAMS, null);
            variables.put(VARIABLE_HEADERS, null);
            variables.put(VARIABLE_COOKIES, null);
            variables.put(VARIABLE_SESSION, null);
        }

        variables.put(VARIABLE_LOGGER, GROOVY_SCRIPT_LOGGER);
        variables.put(VARIABLE_LOCALE, LocaleContextHolder.getLocale());

        addSiteContextVariable(variables);
        addSiteConfigVariable(variables);
    }

    private static void addTemplateModelVariable(Map<String, Object> variables, Object model) {
        variables.put(VARIABLE_MODEL, model);
        variables.put(VARIABLE_TEMPLATE_MODEL, model);
    }

    private static void addContentModelVariable(Map<String, Object> variables, SiteItem siteItem) {
        variables.put(VARIABLE_CRAFTER_MODEL, siteItem);
        variables.put(VARIABLE_CONTENT_MODEL, siteItem);
    }

    private static void addSecurityVariables(Map<String, Object> variables) {
        Authentication auth = SecurityUtils.getCurrentAuthentication();
        Profile profile = null;

        if (auth != null) {
            profile = auth.getProfile();
        }

        variables.put(VARIABLE_AUTH, auth);
        variables.put(VARIABLE_PROFILE, profile);
    }

    private static void addSiteContextVariable(Map<String, Object> variables) {
        variables.put(VARIABLE_SITE_CONTEXT, SiteContext.getCurrent());
    }

    private static void addSiteConfigVariable(Map<String, Object> variables) {
        SiteContext siteContext = SiteContext.getCurrent();
        Configuration config = null;

        if (siteContext != null) {
            config = siteContext.getConfig();
        }

        variables.put(VARIABLE_SITE_CONFIG, config);
    }

    private static void addFilterChainVariable(Map<String, Object> variables, FilterChain filterChain) {
        variables.put(VARIABLE_FILTER_CHAIN, filterChain);
    }

}
