/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.spring.properties;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.List;

/**
 * String {@link org.springframework.beans.factory.FactoryBean} implementation to provide
 * a convenience way to fallback in a list of properties until one of them is not empty/null.
 */
public class FallbackPropertyFactory<T> extends AbstractFactoryBean<T> {

    @Autowired
    private PropertySourcesPropertyResolver propertyResolver;

    private final List<String> properties;
    private final Class<T> type;
    private T defaultValue;

    public FallbackPropertyFactory(final List<String> properties, final Class<T> type) {
        this.properties = properties;
        this.type = type;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    protected T createInstance() throws Exception {
        for (String propertyName : properties) {
            T propertyValue = propertyResolver.getProperty(propertyName, type);
            if (ObjectUtils.isNotEmpty(propertyValue)) {
                return propertyValue;
            }
        }
        return defaultValue;
    }
}
