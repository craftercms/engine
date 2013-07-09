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
package org.craftercms.crafter.engine.macro.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * Represents a {webapp.context.path} macro, which resolves to the web application context path.
 *
 * @author Alfonso Vásquez
 */
public class WebAppContextPathMacro extends AbstractMacro implements ServletContextAware {

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected String createMacroName() {
        return "{webapp.context.path}";
    }

    @Override
    protected String getMacroValue(String str) {
        if (servletContext == null) {
            throw new IllegalStateException("No ServletContext was set. Are you sure you're running in a servlet environment?");
        }

        return StringUtils.stripEnd(servletContext.getContextPath(), "/");
    }

}
