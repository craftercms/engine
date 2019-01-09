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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.engine.http.ExceptionHandler;

/**
 * Handles {@link javax.servlet.ServletException}s that are thrown when a view can't be resolved.
 *
 * @author Alfonso Vásquez
 */
public class ViewNotResolvedExceptionHandler implements ExceptionHandler {

    private static final Log logger = LogFactory.getLog(ViewNotResolvedExceptionHandler.class);
    private static final Pattern viewNotResolvedMsgPattern = Pattern.compile("Could not resolve view with name " +
                                                                             "'(.+)' in servlet with name '(.+)'");

    @Override
    public boolean handle(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        if (ex instanceof ServletException) {
            Matcher viewNotResolvedMsgMatcher = viewNotResolvedMsgPattern.matcher(ex.getMessage());
            if (viewNotResolvedMsgMatcher.matches()) {
                logger.warn("Resource '" + viewNotResolvedMsgMatcher.group(1) + "' not found");

                response.sendError(HttpServletResponse.SC_NOT_FOUND);

                return true;
            }
        }

        return false;
    }

}
