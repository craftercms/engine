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

package org.craftercms.engine.util.spring;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;

/**
 * Utility methods for handling Spring bean definition.
 *
 * @author avasquez
 */
public class BeanDefinitionUtils {

    private BeanDefinitionUtils() {
    }

    /**
     * Creates a bean definition for the specified bean name. If the parent context of the current context contains a
     * bean definition with the same name, the definition is created as a bean copy of the parent definition. This
     * method is useful for config parsers that want to create a bean definition from configuration but also want to
     * retain the default properties of the original bean.
     *
     * @param applicationContext    the current application context
     * @param beanName
     * @return the bean definition
     */
    public static BeanDefinition createBeanDefinitionFromOriginal(ApplicationContext applicationContext,
                                                                  String beanName) {
        ApplicationContext parentContext = applicationContext.getParent();
        BeanDefinition parentDefinition = null;

        if (parentContext != null &&
            parentContext.getAutowireCapableBeanFactory() instanceof ConfigurableListableBeanFactory) {
            ConfigurableListableBeanFactory parentBeanFactory = (ConfigurableListableBeanFactory)parentContext
                .getAutowireCapableBeanFactory();

            try {
                parentDefinition = parentBeanFactory.getBeanDefinition(beanName);
            } catch (NoSuchBeanDefinitionException e) {}
        }

        if (parentDefinition != null) {
            return new GenericBeanDefinition(parentDefinition);
        } else {
            return new GenericBeanDefinition();
        }
    }

    /**
     * Adds the property to the bean definition if the values it not empty
     *
     * @param definition    the bean definition
     * @param propertyName  the property name
     * @param propertyValue the property value
     */
    public static void addPropertyIfNotNull(BeanDefinition definition, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            definition.getPropertyValues().add(propertyName, propertyValue);
        }
    }

}
