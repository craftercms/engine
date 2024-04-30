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
package org.craftercms.engine.servlet.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.craftercms.engine.http.ExceptionHandler;

/**
 * Top level filter used for handling uncaught and unhandled exceptions within the code.
 *
 * @author Alfonso Vásquez
 */
public class ExceptionHandlingFilter implements Filter {

    private List<ExceptionHandler> exceptionHandlers;

    public ExceptionHandlingFilter(List<ExceptionHandler> exceptionHandlers) {
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
