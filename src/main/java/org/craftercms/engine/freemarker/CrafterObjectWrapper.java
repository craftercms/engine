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

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.util.ContentModelUtils;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.engine.model.Dom4jNodeModel;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Extends {@link freemarker.template.DefaultObjectWrapper} to wrap Dom4j {@code Node}s. If it's an {@code Element},
 * then the element will be attempted to be converted based on the content model field conversion algorithm.
 *
 * @author Alfonso Vásquez
 */
public class CrafterObjectWrapper extends DefaultObjectWrapper {

    protected final boolean enableSandbox;

    public CrafterObjectWrapper(boolean enableSandbox) {
        super(Configuration.VERSION_2_3_30);
        this.enableSandbox = enableSandbox;
    }

    @Override
    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof Element && !SiteProperties.isDisableFullModelTypeConversion()) {
            Object result = ContentModelUtils.convertField((Element)obj);
            if (result instanceof Node) {
                return new Dom4jNodeModel((Node)obj, this);
            } else {
                return super.wrap(result);
            }
        } else if (obj instanceof Node) {
            return new Dom4jNodeModel((Node)obj, this);
        } else {
            return super.wrap(obj);
        }
    }

    @Override
    protected TemplateModel invokeMethod(Object object, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException, TemplateModelException {
        if (enableSandbox) {
            boolean blocked;

            if (Modifier.isStatic(method.getModifiers())) {
                blocked = StaticWhitelist.isPermanentlyBlacklistedStaticMethod(method);
            } else {
                blocked = StaticWhitelist.isPermanentlyBlacklistedMethod(method);
            }

            if (blocked) {
                return null;
            }
        }

        return super.invokeMethod(object, method, args);
    }

}
