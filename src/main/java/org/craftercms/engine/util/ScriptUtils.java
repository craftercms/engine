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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.Item;
import org.craftercms.core.util.CollectionUtils;
import org.craftercms.core.util.HttpServletUtils;
import org.craftercms.core.util.UrlUtils;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.security.api.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for scripts.
 *
 * @author Alfonso Vásquez
 */
public class ScriptUtils {

    private static final Log logger = LogFactory.getLog(ScriptUtils.class);

    public static final String VARIABLE_REQUEST_URL =               "requestUrl";
    public static final String VARIABLE_APPLICATION =               "application";
    public static final String VARIABLE_REQUEST =                   "request";
    public static final String VARIABLE_RESPONSE =                  "response";
    public static final String VARIABLE_PARAMS =                    "params";
    public static final String VARIABLE_HEADERS =                   "headers";
    public static final String VARIABLE_COOKIES =                   "cookies";
    public static final String VARIABLE_SESSION =                   "session";
    public static final String VARIABLE_LOGGER =                    "logger";
    public static final String VARIABLE_MODEL =                     "model";
    public static final String VARIABLE_CRAFTER_MODEL =             "crafterModel";
    public static final String VARIABLE_CRAFTER_REQUEST_CONTEXT =   "crafterRequestContext";
    public static final String VARIABLE_PROFILE =                   "profile";

    public static void addCommonVariables(Map<String, Object> variables, HttpServletRequest request, HttpServletResponse response,
                                          ServletContext context) {
        variables.put(VARIABLE_REQUEST_URL, request.getRequestURI());
        variables.put(VARIABLE_APPLICATION, context);
        variables.put(VARIABLE_REQUEST, request);
        variables.put(VARIABLE_RESPONSE, response);
        variables.put(VARIABLE_PARAMS, HttpServletUtils.createRequestParamsMap(request));
        variables.put(VARIABLE_HEADERS, HttpServletUtils.createHeadersMap(request));
        variables.put(VARIABLE_COOKIES, HttpServletUtils.createCookiesMap(request));
        variables.put(VARIABLE_SESSION, request.getSession(false));
        variables.put(VARIABLE_LOGGER, logger);
    }

    public static void addModelVariable(Map<String, Object> variables, Object model) {
        variables.put(VARIABLE_MODEL, model);
    }

    public static void addCrafterVariables(Map<String, Object> variables) {
        RequestContext context = RequestContext.getCurrent();
        if (context != null) {
            variables.put(VARIABLE_CRAFTER_REQUEST_CONTEXT, context);

            if (context.getAuthenticationToken() != null && context.getAuthenticationToken().getProfile() != null) {
                variables.put(VARIABLE_PROFILE, context.getAuthenticationToken().getProfile());
            }
        } else {
            variables.put(VARIABLE_CRAFTER_REQUEST_CONTEXT, null);
            variables.put(VARIABLE_PROFILE, null);
        }
    }

    public static void addCrafterVariables(Map<String, Object> variables, SiteItem crafterModel) {
        variables.put(VARIABLE_CRAFTER_MODEL, crafterModel);

        addCrafterVariables(variables);
    }

}
