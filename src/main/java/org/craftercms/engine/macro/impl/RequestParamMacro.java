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
package org.craftercms.engine.macro.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.RequestContext;

/**
 * Represents a macro that can be a request parameter.
 *
 * @author Alfonso Vásquez
 */
public class RequestParamMacro extends AbstractMacro {

    private static final Log logger = LogFactory.getLog(RequestParamMacro.class);

    private String requestParamName;

    public RequestParamMacro(String requestParamName) {
        this.requestParamName = requestParamName;
    }

    @Override
    protected String createMacroName() {
        return "{" + requestParamName + "}";
    }

    @Override
    protected String getMacroValue(String str) {
        RequestContext requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            return requestContext.getRequest().getParameter(requestParamName);
        } else {
            return null;
        }
    }

}

