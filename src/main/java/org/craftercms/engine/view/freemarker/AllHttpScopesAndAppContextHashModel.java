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

    private boolean disableVariableRestrictions;

    public AllHttpScopesAndAppContextHashModel(ObjectWrapper wrapper,
                                               ApplicationContextAccessor applicationContextAccessor,
                                               ServletContext context, HttpServletRequest request,
                                               boolean disableVariableRestrictions) {
        super(wrapper);

        this.applicationContextAccessor = applicationContextAccessor;
        this.context = context;
        this.request = request;
        this.disableVariableRestrictions = disableVariableRestrictions;
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

        if (disableVariableRestrictions) {
            // Lookup in application scope
            obj = context.getAttribute(key);
            if (obj != null) {
                return wrap(obj);
            }
        }

        // Lookup in application context
        try {
            return wrap(applicationContextAccessor.get(key));
        } catch (NoSuchBeanDefinitionException e) {
            // do nothing...
        }

        // return wrapper's null object (probably null).
        return wrap(null);
    }

}
