/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.model;

import org.craftercms.core.service.Item;
import org.craftercms.engine.exception.ScriptException;
import org.dom4j.Document;

import java.util.Map;

import static java.lang.String.format;
import static org.craftercms.engine.exception.ScriptException.MODIFY_PROPERTY_EXCEPTION_FORMAT;

/**
 * Immutable implementation of {@link Item} class
 */
public final class ImmutableItem extends Item {
    private final String name;
    private final String url;
    private final String descriptorUrl;
    private final Document descriptorDom;
    private final Map<String, Object> properties;
    private final boolean isFolder;
    private ImmutableItem(String name, String url, String descriptorUrl, Document descriptorDom,
                          Map<String, Object> properties, boolean isFolder) {
        this.name = name;
        this.url = url;
        this.descriptorUrl = descriptorUrl;
        this.descriptorDom = descriptorDom;
        this.properties = properties;
        this.isFolder = isFolder;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        throw new ScriptException(format(MODIFY_PROPERTY_EXCEPTION_FORMAT, "name"));
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        throw new ScriptException(format(MODIFY_PROPERTY_EXCEPTION_FORMAT, "url"));
    }

    @Override
    public String getDescriptorUrl() {
        return descriptorUrl;
    }

    @Override
    public void setDescriptorUrl(String descriptorUrl) {
        throw new ScriptException(format(MODIFY_PROPERTY_EXCEPTION_FORMAT, "descriptorUrl"));
    }

    @Override
    public Document getDescriptorDom() {
        return descriptorDom;
    }

    @Override
    public void setDescriptorDom(Document descriptorDom) {
        throw new ScriptException(format(MODIFY_PROPERTY_EXCEPTION_FORMAT, "descriptorDom"));
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        throw new ScriptException(format(MODIFY_PROPERTY_EXCEPTION_FORMAT, "properties"));
    }

    @Override
    public boolean isFolder() {
        return isFolder;
    }

    @Override
    public void setFolder(boolean folder) {
        throw new ScriptException(format(MODIFY_PROPERTY_EXCEPTION_FORMAT, "folder"));
    }

    @Override
    public void setProperty(String key, Object value) {
        throw new ScriptException(format(MODIFY_PROPERTY_EXCEPTION_FORMAT, key));
    }

    public static ImmutableItem copyOf(Item item) {
        return new ImmutableItem(
                item.getName(),
                item.getUrl(),
                item.getDescriptorUrl(),
                item.getDescriptorDom(),
                item.getProperties(),
                item.isFolder()
        );
    }
}
