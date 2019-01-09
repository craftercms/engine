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
package org.craftercms.engine.macro.impl;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;

/**
 * Represents a {request.uri} macro, which resolves to the current request uri.
 *
 * @author Alfonso Vásquez
 */
public class RequestUriMacro extends AbstractMacro {

    @Override
    protected String createMacroName() {
        return "{request.uri}";
    }

    @Override
    protected String getMacroValue(String str) {
        RequestContext requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            String requestUri = requestContext.getRequest().getRequestURI();

            if (!requestUri.startsWith("/")) {
                requestUri = "/" + requestUri;
            }
            if (!requestUri.equals("/")) {
                requestUri = StringUtils.stripEnd(requestUri, "/");
            }

            return requestUri;
        } else {
            return null;
        }
    }

}
