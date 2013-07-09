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
package org.craftercms.engine.http.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.engine.exception.HttpStatusCodeException;
import org.craftercms.engine.http.ExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handler for {@code HttpStatusCodeException}s.
 *
 * @author Alfonso Vásquez
 */
public class HttpStatusCodeExceptionHandler implements ExceptionHandler {

    private static final Log logger = LogFactory.getLog(HttpStatusCodeExceptionHandler.class);

    @Override
    public boolean handle(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        if (ex instanceof ServletException && ex.getCause() instanceof Exception) {
            ex = (Exception) ex.getCause();
        }

        if (ex instanceof HttpStatusCodeException ) {
            logger.warn(ex.getMessage(), ex);

            response.sendError(((HttpStatusCodeException) ex).getStatusCode().value());

            return true;
        }

        return false;
    }

}
