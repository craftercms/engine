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

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.util.ContentModelUtils;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.engine.model.Dom4jNodeModel;

/**
 * Extends {@link freemarker.template.DefaultObjectWrapper} to wrap Dom4j {@code Node}s. If it's an {@code Element},
 * then the element will be attempted to be converted based on the content model field conversion algorithm.
 *
 * @author Alfonso Vásquez
 */
public class CrafterObjectWrapper extends DefaultObjectWrapper {

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

}
