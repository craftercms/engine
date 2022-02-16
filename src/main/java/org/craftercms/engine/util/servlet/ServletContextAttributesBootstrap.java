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

package org.craftercms.engine.util.servlet;

import java.util.Map;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * A simple class that adds a bunch of attributes passed as a property to the current servlet context.
 *
 * @author avasquez
 */
public class ServletContextAttributesBootstrap implements ServletContextAware, InitializingBean {

    private ServletContext servletContext;
    private Map<String, Object> attributes;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Required
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void afterPropertiesSet() {
        if (servletContext == null) {
            throw new IllegalStateException("There's no current ServletContext");
        }

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            servletContext.setAttribute(entry.getKey(), entry.getValue());
        }
    }

}
