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
package org.craftercms.engine.scripting;

import org.craftercms.core.util.HttpServletUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for scripts.
 *
 * @author Alfonso Vásquez
 */
public class ScriptUtils {

    public static Map<String, Object> createServletVariables(HttpServletRequest request, HttpServletResponse response,
                                                             ServletContext context) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("requestUrl", request.getRequestURI());
        variables.put("application", context);
        variables.put("request", request);
        variables.put("response", response);
        variables.put("params", HttpServletUtils.createRequestParamsMap(request));
        variables.put("headers", HttpServletUtils.createHeadersMap(request));
        variables.put("cookies", HttpServletUtils.createCookiesMap(request));
        variables.put("session", request.getSession(false));
        variables.put("sessionAttributes", HttpServletUtils.createSessionMap(request));

        return variables;
    }

}
