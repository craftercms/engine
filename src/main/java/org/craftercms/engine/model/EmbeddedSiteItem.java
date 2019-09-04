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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.craftercms.commons.converters.Converter;
import org.craftercms.core.service.Item;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Implementation of {@link SiteItem} for embedded site items
 *
 * @author joseross
 * @since 3.1.2
 */
public class EmbeddedSiteItem extends AbstractXmlSiteItem {

    /**
     * The root XML element of the embedded component
     */
    protected Element rootElement;

    public EmbeddedSiteItem(final Element rootElement, final Converter<Element, Object> modelFieldConverter) {
        super(modelFieldConverter);
        this.rootElement = rootElement;
    }

    @Override
    protected Element getRootElement() {
        return rootElement;
    }

    @Override
    public Item getItem() {
        return null;
    }

    @Override
    public String getStoreName() {
        return null;
    }

    @Override
    public String getStoreUrl() {
        return null;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public Document getDom() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public List<SiteItem> getChildItems() {
        return null;
    }

    @Override
    public SiteItem getChildItem(final String storeName) {
        return null;
    }

    @Override
    public List<SiteItem> sortItems(final List<SiteItem> items, final Comparator<SiteItem> comparator) {
        return null;
    }

    @Override
    public SiteItem createItemWrapper(final Item item) {
        return null;
    }

    @Override
    public String toString() {
        return "EmbeddedSiteItem{" + "rootElement=" + rootElement + '}';
    }

}
