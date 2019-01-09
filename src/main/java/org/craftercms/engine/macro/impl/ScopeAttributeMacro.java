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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.craftercms.commons.http.RequestContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;

/**
 * Represents a macro that can be an attribute from the servlet context, session or request scope.
 *
 * @author Alfonso Vásquez
 */
public class ScopeAttributeMacro extends AbstractMacro implements ServletContextAware {

    public enum Scope {
        SERVLET_CONTEXT,
        SESSION,
        REQUEST
    }

    private String attributeName;
    private Scope scope;
    private ServletContext servletContext;

    public ScopeAttributeMacro() {
        scope = Scope.REQUEST;
    }

    @Required
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected String createMacroName() {
        return "{" + attributeName + "}";
    }

    @Override
    protected String getMacroValue(String str) {
        switch (scope) {
            case SERVLET_CONTEXT:
                return getServletContextAttribute();
            case SESSION:
                return getSessionAttribute();
            default:
                return getRequestAttribute();
        }
    }

    private String getServletContextAttribute() {
        return getAttributeAsString(servletContext.getAttribute(attributeName));
    }

    private String getSessionAttribute() {
        RequestContext requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            HttpSession session = requestContext.getRequest().getSession();
            if (session != null) {
                return getAttributeAsString(session.getAttribute(attributeName));
            }
        }

        return null;
    }

    private String getRequestAttribute() {
        RequestContext requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            return getAttributeAsString(requestContext.getRequest().getAttribute(attributeName));
        }

        return null;
    }

    private String getAttributeAsString(Object attribute) {
        if (attribute != null) {
            return attribute.toString();
        } else {
            return null;
        }
    }

}
