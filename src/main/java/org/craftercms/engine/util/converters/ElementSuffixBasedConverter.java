/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.converters;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.converters.Converter;
import org.craftercms.engine.properties.SiteProperties;
import org.dom4j.Element;

public class ElementSuffixBasedConverter implements Converter<Element, Object> {

    private static final Log logger = LogFactory.getLog(ElementSuffixBasedConverter.class);

    public static final String[] DEFAULT_SUPPORTED_SUFFIXES_ON_DISABLED_FULL_MODEL_CONVERSION =  { "i", "l", "b", "f", "d" };

    protected Map<String, Converter<String, ?>> suffixMappedConverters;
    protected String[] supportedSuffixesOnDisabledFullModelTypeConversion;

    public ElementSuffixBasedConverter(Map<String, Converter<String, ?>> suffixMappedConverters) {
        supportedSuffixesOnDisabledFullModelTypeConversion = DEFAULT_SUPPORTED_SUFFIXES_ON_DISABLED_FULL_MODEL_CONVERSION;

        this.suffixMappedConverters = suffixMappedConverters;
    }

    public void setSupportedSuffixesOnDisabledFullModelTypeConversion(String[] supportedSuffixesOnDisabledFullModelTypeConversion) {
        this.supportedSuffixesOnDisabledFullModelTypeConversion = supportedSuffixesOnDisabledFullModelTypeConversion;
    }

    @Override
    public Class<?> getSourceClass() {
        return Element.class;
    }

    @Override
    public Class<?> getTargetClass() {
        return Object.class;
    }

    @Override
    public Object convert(Element source) {
        String name = source.getName();
        int converterIdSuffixSepIdx = name.lastIndexOf("_");

        if (converterIdSuffixSepIdx >= 0) {
            String converterId = name.substring(converterIdSuffixSepIdx + 1);
            Converter<String, ?> converter = suffixMappedConverters.get(converterId);

            if (converter != null) {
                if (!SiteProperties.isDisableFullModelTypeConversion() ||
                    ArrayUtils.contains(supportedSuffixesOnDisabledFullModelTypeConversion, converterId)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Converting value of <" + name + "> to " + converter.getTargetClass().getName());
                    }

                    return converter.convert(source.getText());
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("No converter found for suffix '" + converterId + "' for <" + name + ">");
            }
        }

        return source;
    }

}
