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
package org.craftercms.engine.freemarker;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import jakarta.servlet.ServletContext;

/**
 * Template model for the {@link ServletContext}
 *
 * @author Alfonso Vásquez
 */
public class ServletContextHashModel implements TemplateHashModel {

    private ServletContext servletContext;
    private ObjectWrapper objectWrapper;

    public ServletContextHashModel(ServletContext servletContext, ObjectWrapper objectWrapper) {
        this.servletContext = servletContext;
        this.objectWrapper = objectWrapper;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        return objectWrapper.wrap(servletContext.getAttribute(key));
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return !servletContext.getAttributeNames().hasMoreElements();
    }

}
