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
package org.craftercms.engine.servlet.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.engine.http.ExceptionHandler;
import org.springframework.beans.factory.annotation.Required;

/**
 * Top level filter used for handling uncaught and unhandled exceptions within the code.
 *
 * @author Alfonso Vásquez
 */
public class ExceptionHandlingFilter implements Filter {

    private List<ExceptionHandler> exceptionHandlers;

    @Required
    public void setExceptionHandlers(List<ExceptionHandler> exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            handleException((HttpServletRequest) request, (HttpServletResponse) response, e);
        }
    }

    @Override
    public void destroy() {
    }

    protected void handleException(HttpServletRequest request, HttpServletResponse response, Exception ex)
        throws ServletException, IOException {
        boolean handled = false;

        for (Iterator<ExceptionHandler> iter = exceptionHandlers.iterator(); iter.hasNext() && !handled;) {
            handled = iter.next().handle(request, response, ex);
        }

        if (!handled) {
            throw new ServletException("Unhandled exception: " + ex.getMessage(), ex);
        }
    }

}
