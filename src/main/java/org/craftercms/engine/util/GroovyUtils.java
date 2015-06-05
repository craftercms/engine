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
package org.craftercms.engine.util;

import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.commons.http.HttpUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for Groovy scripts and classes.
 *
 * @author Alfonso Vásquez
 */
public class GroovyUtils {

    public static final Logger LOGGER = LoggerFactory.getLogger(GroovyUtils.class);

    public static final String VARIABLE_REQUEST_URL = "requestUrl";
    public static final String VARIABLE_APPLICATION = "application";
    public static final String VARIABLE_REQUEST = "request";
    public static final String VARIABLE_RESPONSE = "response";
    public static final String VARIABLE_PARAMS = "params";
    public static final String VARIABLE_HEADERS = "headers";
    public static final String VARIABLE_COOKIES = "cookies";
    public static final String VARIABLE_SESSION = "session";
    public static final String VARIABLE_LOGGER = "logger";
    public static final String VARIABLE_MODEL = "model";
    public static final String VARIABLE_CRAFTER_MODEL = "crafterModel";
    public static final String VARIABLE_AUTH = "authentication";
    public static final String VARIABLE_PROFILE = "profile";
    public static final String VARIABLE_FILTER_CHAIN = "filterChain";

    private GroovyUtils() {
    }

    public static void addCommonVariables(Map<String, Object> variables, HttpServletRequest request,
                                          HttpServletResponse response, ServletContext context) {
        variables.put(VARIABLE_REQUEST_URL, request.getRequestURI());
        variables.put(VARIABLE_APPLICATION, context);
        variables.put(VARIABLE_REQUEST, request);
        variables.put(VARIABLE_RESPONSE, response);
        variables.put(VARIABLE_PARAMS, HttpUtils.createRequestParamsMap(request));
        variables.put(VARIABLE_HEADERS, HttpUtils.createHeadersMap(request));
        variables.put(VARIABLE_COOKIES, HttpUtils.createCookiesMap(request));
        variables.put(VARIABLE_SESSION, request.getSession(false));
        variables.put(VARIABLE_LOGGER, LOGGER);
    }

    public static void addModelVariable(Map<String, Object> variables, Object model) {
        variables.put(VARIABLE_MODEL, model);
    }

    public static void addSecurityVariables(Map<String, Object> variables) {
        RequestContext context = RequestContext.getCurrent();
        Authentication auth = null;
        Profile profile = null;

        if (context != null) {
            auth = SecurityUtils.getAuthentication(context.getRequest());
            if (auth != null) {
                profile = auth.getProfile();
            }
        }

        variables.put(VARIABLE_AUTH, auth);
        variables.put(VARIABLE_PROFILE, profile);
    }

    public static void addCrafterVariables(Map<String, Object> variables, SiteItem crafterModel) {
        variables.put(VARIABLE_CRAFTER_MODEL, crafterModel);

        addSecurityVariables(variables);
    }

    public static void addFilterChainVariable(Map<String, Object> variables, FilterChain filterChain) {
        variables.put(VARIABLE_FILTER_CHAIN, filterChain);
    }

}
