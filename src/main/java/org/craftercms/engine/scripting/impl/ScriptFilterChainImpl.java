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

package org.craftercms.engine.scripting.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.util.GroovyScriptUtils;

/**
 * {@link FilterChain} implementation that executes a chain of scripts, before delegating to the actual servlet
 * filter chain.
 *
 * @author avasquez
 */
public class ScriptFilterChainImpl implements FilterChain {

    private static final Log logger = LogFactory.getLog(ScriptFilterChainImpl.class);

    private Iterator<Script> scriptIterator;
    private FilterChain delegateChain;
    private ServletContext servletContext;

    public ScriptFilterChainImpl(Iterator<Script> scriptIterator, FilterChain delegateChain,
                                 ServletContext servletContext) {
        this.scriptIterator = scriptIterator;
        this.delegateChain = delegateChain;
        this.servletContext = servletContext;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (scriptIterator.hasNext()){
            Script script = scriptIterator.next();

            if (logger.isDebugEnabled()) {
                logger.debug("Executing filter script at " + script.getUrl());
            }

            HttpServletRequest httpRequest = (HttpServletRequest)request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            Map<String, Object> variables = new HashMap<>();

            GroovyScriptUtils.addFilterScriptVariables(variables, httpRequest, httpResponse, servletContext, this);

            try {
                script.execute(variables);
            } catch (ScriptException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ServletException) {
                    throw (ServletException)cause;
                } else {
                    throw new ServletException("Error executing filter script at " + script.getUrl(), cause);
                }
            }
        } else {
            delegateChain.doFilter(request, response);
        }
    }

}
