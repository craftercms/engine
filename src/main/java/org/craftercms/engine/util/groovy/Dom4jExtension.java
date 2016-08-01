package org.craftercms.engine.util.groovy;

import org.craftercms.core.util.XmlUtils;
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
     * {@code siteItem.collection.item[1].text}.
     *
     * @param node              the Node object (this)
     * @param xpathExpression   the XPath expression or query
     *
     * @return the result of the XPath query
     */
    public static Object get(Node node, String xpathExpression) {
        return XmlUtils.selectObject(node, xpathExpression);
    }

}
