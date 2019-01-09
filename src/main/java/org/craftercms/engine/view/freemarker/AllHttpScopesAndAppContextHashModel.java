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
package org.craftercms.engine.view.freemarker;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.craftercms.engine.util.spring.ApplicationContextAccessor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Like {@link freemarker.ext.servlet.AllHttpScopesHashModel}, but also lookup keys in the Application Context.
 *
 * @author Alfonso Vásquez
 */
public class AllHttpScopesAndAppContextHashModel extends SimpleHash {

    private ApplicationContextAccessor applicationContextAccessor;
    private ServletContext context;
    private HttpServletRequest request;

    public AllHttpScopesAndAppContextHashModel(ObjectWrapper wrapper,
                                               ApplicationContextAccessor applicationContextAccessor,
                                               ServletContext context, HttpServletRequest request) {
        super(wrapper);

        this.applicationContextAccessor = applicationContextAccessor;
        this.context = context;
        this.request = request;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        // Lookup in page scope
        TemplateModel model = super.get(key);
        if (model != null) {
            return model;
        }

        // Lookup in request scope
        Object obj = request.getAttribute(key);
        if (obj != null) {
            return wrap(obj);
        }

        // Lookup in session scope
        HttpSession session = request.getSession(false);
        if (session != null) {
            obj = session.getAttribute(key);
            if (obj != null) {
                return wrap(obj);
            }
        }

        // Lookup in application scope
        obj = context.getAttribute(key);
        if (obj != null) {
            return wrap(obj);
        }

        // Lookup in application context
        try {
            return wrap(applicationContextAccessor.get(key));
        } catch (NoSuchBeanDefinitionException e) {
        }

        // return wrapper's null object (probably null).
        return wrap(null);
    }

}
