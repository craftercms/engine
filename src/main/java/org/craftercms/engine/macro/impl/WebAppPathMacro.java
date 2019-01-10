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

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.ServletContextAware;

/**
 * Represents a {webapp.path} macro, which resolves to the web app exploded dir path of the current web app.
 *
 * @author Alfonso Vásquez
 */
public class WebAppPathMacro extends AbstractMacro implements ServletContextAware {

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected String createMacroName() {
        return "{webapp.path}";
    }

    @Override
    protected String getMacroValue(String str) {
        if (servletContext != null) {
            String webAppPath = servletContext.getRealPath("/");
            webAppPath = StringUtils.stripEnd(webAppPath, "/");

            return webAppPath;
        } else {
            return null;
        }
    }

}
