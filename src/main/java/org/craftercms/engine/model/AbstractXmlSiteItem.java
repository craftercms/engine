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

package org.craftercms.engine.model;

import java.util.List;
import java.util.Map;

import org.craftercms.commons.converters.Converter;
import org.craftercms.core.util.XmlUtils;
import org.dom4j.Element;

/**
 * Base implementation of {@link SiteItem} that handles all XML related operations
 *
 * @author joseross
 * @since 3.1.2
 */
public abstract class AbstractXmlSiteItem implements SiteItem {

    protected Converter<Element, Object> modelFieldConverter;

    public AbstractXmlSiteItem(Converter<Element, Object> modelFieldConverter) {
        this.modelFieldConverter = modelFieldConverter;
    }

    protected abstract Element getRootElement();

    @Override
    public Object get(String xpathExpression) {
        if (getRootElement() != null) {
            Object result = XmlUtils.selectObject(getRootElement(), xpathExpression);
            if (result instanceof Element) {
                return modelFieldConverter.convert((Element) result);
            } else {
                return result;
            }
        } else {
            return null;
        }
    }

    @Override
    public String queryValue(String xpathExpression) {
        if (getRootElement() != null) {
            return XmlUtils.selectSingleNodeValue(getRootElement(), xpathExpression);
        } else {
            return null;
        }
    }

    @Override
    public List<String> queryValues(String xpathExpression) {
        if (getRootElement() != null) {
            return XmlUtils.selectNodeValues(getRootElement(), xpathExpression);
        } else {
            return null;
        }
    }

    @Override
    public String queryValue(String xpathExpression, Map<String, String> namespaceUris) {
        if (getRootElement() != null) {
            return XmlUtils.selectSingleNodeValue(getRootElement(), xpathExpression, namespaceUris);
        } else {
            return null;
        }
    }

    @Override
    public List<String> queryValues(String xpathExpression, Map<String, String> namespaceUris) {
        if (getRootElement() != null) {
            return XmlUtils.selectNodeValues(getRootElement(), xpathExpression, namespaceUris);
        } else {
            return null;
        }
    }

}
