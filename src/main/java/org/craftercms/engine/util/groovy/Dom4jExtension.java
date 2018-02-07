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
     * {@code siteItem.collection.item[1].text}. Also, if the result is an {@code Element}, and is text only, then
     * the element will be attempted to be converted based on the content model field conversion algorithm.
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
