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
package org.craftercms.engine.util.groovy;

import org.craftercms.core.util.XmlUtils;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.util.ContentModelUtils;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Groovy extension module for Dom4j
 *
 * @author avasquez
 * @see <a href="http://groovy-lang.org/metaprogramming.html#_extension_modules">Extension Modules</a>
 */
public class Dom4jExtension {

    /**
     * Adds a get method to Dom4j nodes, which allows XPath queries through dot notation properties, e.g
     * {@code siteItem.collection.item[1].text}. Also, if the result is an {@code Element}, then the
     * element will attempted to be converted based on the content model field conversion algorithm.
     *
     * @param node              the Node object (this)
     * @param xpathExpression   the XPath expression or query
     *
     * @return the result of the XPath query
     */
    public static Object get(Node node, String xpathExpression) {
        Object result = XmlUtils.selectObject(node, xpathExpression);
        if (result != null) {
            if (result instanceof Element && !SiteProperties.isDisableFullModelTypeConversion()) {
                return ContentModelUtils.convertField((Element)result);
            } else {
                return result;
            }
        } else {
            return null;
        }
    }

}
