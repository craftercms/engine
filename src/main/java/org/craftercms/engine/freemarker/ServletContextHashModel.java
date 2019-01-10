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
package org.craftercms.engine.freemarker;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.servlet.ServletContext;

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
