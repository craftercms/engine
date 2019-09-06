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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.craftercms.commons.converters.Converter;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.Tree;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Basic adapter to a {@link Item}, enhanced with methods that can be easily invoked in template engines like Freemarker.
 * The generic get method allows things like ${model.header.title} in Freemarker.
 *
 * @author Alfonso Vásquez
 */
public class DefaultSiteItem extends AbstractXmlSiteItem {

    protected Item item;
    protected List<SiteItem> childItems;
    protected Comparator<SiteItem> sortComparator;

    public DefaultSiteItem(Item item, Converter<Element, Object> modelFieldConverter,
                        Comparator<SiteItem> sortComparator) {
        super(modelFieldConverter);
        this.item = item;
        this.sortComparator = sortComparator;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public String getStoreName() {
        return item.getName();
    }

    @Override
    public String getStoreUrl() {
        return item.getUrl();
    }

    @Override
    public boolean isFolder() {
        return item.isFolder();
    }

    @Override
    public Document getDom() {
        return item.getDescriptorDom();
    }

    @Override
    public Map<String, Object> getProperties() {
        return item.getProperties();
    }

    @Override
    protected Element getRootElement() {
        if (getDom() != null) {
            return getDom().getRootElement();
        } else {
            return null;
        }
    }

    @Override
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

    @Override
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

    @Override
    public List<SiteItem> sortItems(List<SiteItem> items, Comparator<SiteItem> comparator) {
        Collections.sort(items, comparator);

        return items;
    }

    @Override
    public SiteItem createItemWrapper(Item item) {
        return new DefaultSiteItem(item, modelFieldConverter, sortComparator);
    }

}
