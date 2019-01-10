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
package org.craftercms.engine.http.impl;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.core.util.ExceptionUtils;
import org.craftercms.engine.exception.HttpStatusCodeAwareException;
import org.craftercms.engine.http.ExceptionHandler;

/**
 * Handler for {@code HttpStatusCodeException}s.
 *
 * @author Alfonso Vásquez
 */
public class HttpStatusCodeAwareExceptionHandler implements ExceptionHandler {

    private static final Log logger = LogFactory.getLog(HttpStatusCodeAwareExceptionHandler.class);

    @Override
    public boolean handle(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        HttpStatusCodeAwareException httpStatusCodeAwareEx =
            ExceptionUtils.getThrowableOfType(ex, HttpStatusCodeAwareException.class);

        if (httpStatusCodeAwareEx != null) {
            ex = (Exception) httpStatusCodeAwareEx;

            logger.error(request.getMethod() + " " + HttpUtils.getFullRequestUri(request, true) + " failed", ex);

            response.sendError(httpStatusCodeAwareEx.getStatusCode(), ex.getMessage());

            return true;
        }

        return false;
    }

}
