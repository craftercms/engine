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
package org.craftercms.engine.util;

import org.craftercms.commons.converters.Converter;
import org.craftercms.engine.service.context.SiteContext;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;

public class ContentModelUtils {

    public static final String FIELD_CONVERTER_BEAN_NAME = "crafter.contentModelFieldConverter";

    private ContentModelUtils() {

    }

    @SuppressWarnings("unchecked")
    public static final Object convertField(Element field) {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            ApplicationContext appContext = siteContext.getGlobalApplicationContext();
            if (appContext != null) {
                Converter<Element, Object> converter = appContext.getBean(FIELD_CONVERTER_BEAN_NAME, Converter.class);
                if (converter != null) {
                    return converter.convert(field);
                }
            }
        }

        return field;
    }

}
