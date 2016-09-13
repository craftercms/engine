/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.Tree;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.engine.model.converters.ModelValueConverter;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Basic adapter to a {@link Item}, enhanced with methods that can be easily invoked in template engines like Freemarker.
 * The generic get method allows things like ${model.header.title} in Freemarker.
 *
 * @author Alfonso Vásquez
 */
public class SiteItem {

    private static final Log logger = LogFactory.getLog(SiteItem.class);

    protected Item item;
    protected List<SiteItem> childItems;
    protected Map<String, ModelValueConverter<?>> modelValueConverters;
    protected Comparator<SiteItem> sortComparator;

    public SiteItem(Item item, Map<String, ModelValueConverter<?>> modelValueConverters,
                    Comparator<SiteItem> sortComparator) {
        this.item = item;
        this.modelValueConverters = modelValueConverters;
        this.sortComparator = sortComparator;
    }

    public Item getItem() {
        return item;
    }

    public String getStoreName() {
        return item.getName();
    }

    public String getStoreUrl() {
        return item.getUrl();
    }

    public boolean isFolder() {
        return item.isFolder();
    }

    public Document getDom() {
        return item.getDescriptorDom();
    }

    public Map<String, Object> getProperties() {
        return item.getProperties();
    }

    public Object get(String xpathExpression) {
        if (getDom() != null) {
            Object result = XmlUtils.selectObject(getDom().getRootElement(), xpathExpression);
            if (result instanceof Element) {
                return convertModelValue((Element) result);
            } else {
                return result;
            }
        } else {
            return null;
        }
    }

    public String queryValue(String xpathExpression) {
        if (getDom() != null) {
            return XmlUtils.selectSingleNodeValue(getDom().getRootElement(), xpathExpression);
        } else {
            return null;
        }
    }

    public List<String> queryValues(String xpathExpression) {
        if (getDom() != null) {
            return XmlUtils.selectNodeValues(getDom().getRootElement(), xpathExpression);
        } else {
            return null;
        }
    }

    public String queryValue(String xpathExpression, Map<String, String> namespaceUris) {
        if (getDom() != null) {
            return XmlUtils.selectSingleNodeValue(getDom().getRootElement(), xpathExpression, namespaceUris);
        } else {
            return null;
        }
    }

    public List<String> queryValues(String xpathExpression, Map<String, String> namespaceUris) {
        if (getDom() != null) {
            return XmlUtils.selectNodeValues(getDom().getRootElement(), xpathExpression, namespaceUris);
        } else {
            return null;
        }
    }

    public List<SiteItem> getChildItems() {
        if (childItems == null) {
            if (item instanceof Tree) {
                List<Item> treeChildren = ((Tree) item).getChildren();
                if (CollectionUtils.isNotEmpty(treeChildren)) {
                    childItems = new ArrayList<>(treeChildren.size());
                    for (Item treeChild : treeChildren) {
                        childItems.add(createItemWrapper(treeChild));
                    }

                    if (sortComparator != null) {
                        childItems = sortItems(childItems, sortComparator);
                    }
                } else {
                    childItems = Collections.emptyList();
                }
            } else {
                childItems = Collections.emptyList();
            }
        }

        return childItems;
    }

    public SiteItem getChildItem(String storeName) {
        for (SiteItem childItem : getChildItems()) {
            if (childItem.getStoreName().equals(storeName)) {
                return childItem;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "SiteItem[" +
                "storeName='" + getStoreName() + '\'' +
                ", storeUrl='" + getStoreUrl() + '\'' +
                ", folder=" + isFolder() +
                ']';
    }

    protected List<SiteItem> sortItems(List<SiteItem> items, Comparator<SiteItem> comparator) {
        Collections.sort(items, comparator);

        return items;
    }

    protected Object convertModelValue(Element element) {
        String name = element.getName();
        int converterIdSuffixSepIdx = name.lastIndexOf("_");

        if (converterIdSuffixSepIdx >= 0) {
            String converterId = name.substring(converterIdSuffixSepIdx + 1);
            ModelValueConverter<?> converter = modelValueConverters.get(converterId);

            if (converter != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Converting value of <" + name + "> @ " + this + " with converter " + converter);
                }

                return converter.convert(element.getText());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No converter found for '" + converterId + "' (<" + name + "> @ " + this + ")");
                }
            }
        }

        return element;
    }

    protected SiteItem createItemWrapper(Item item) {
        return new SiteItem(item, modelValueConverters, sortComparator);
    }

}
