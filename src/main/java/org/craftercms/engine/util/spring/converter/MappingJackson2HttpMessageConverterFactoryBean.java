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
package org.craftercms.engine.util.spring.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Factory bean to create a {@link MappingJackson2HttpMessageConverter} bean with customized {@link ObjectMapper},
 * and enabled JsonParser.Feature.STRICT_DUPLICATE_DETECTION
 *
 * @author Phil Nguyen
 */
public class MappingJackson2HttpMessageConverterFactoryBean extends AbstractFactoryBean<MappingJackson2HttpMessageConverter> {
    private ObjectMapper objectMapper;

    @Override
    public Class<MappingJackson2HttpMessageConverter> getObjectType() {
        return MappingJackson2HttpMessageConverter.class;
    }

    @Override
    protected MappingJackson2HttpMessageConverter createInstance() throws Exception {
        objectMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
