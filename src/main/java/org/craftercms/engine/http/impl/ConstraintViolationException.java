/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.http.impl;

import org.craftercms.core.util.ExceptionUtils;
import org.craftercms.engine.http.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handler for {@link ConstraintViolationException} exceptions
 */
public class ConstraintViolationException implements ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConstraintViolationException.class);

    @Override
    public boolean handle(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        Exception constraintViolationException =
                ExceptionUtils.getThrowableOfType(ex, javax.validation.ConstraintViolationException.class);

        if (constraintViolationException != null) {
            logger.warn("Failed to validate request parameters: {}", constraintViolationException.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, constraintViolationException.getMessage());
            return true;
        }

        return false;
    }
}
