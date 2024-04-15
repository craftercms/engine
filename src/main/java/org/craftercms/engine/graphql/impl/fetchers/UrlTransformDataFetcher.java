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

package org.craftercms.engine.graphql.impl.fetchers;

import java.util.Map;

import graphql.schema.DataFetchingEnvironment;
import org.craftercms.engine.service.UrlTransformationService;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.engine.graphql.SchemaUtils.ARG_NAME_TRANSFORM;

/**
 * Implementation of {@link graphql.schema.DataFetcher} that transforms a field using a {@link UrlTransformationService}
 *
 * @since 3.1.1
 * @author joseross
 */
public class UrlTransformDataFetcher extends RequestAwareDataFetcher {

    /**
     * The {@link UrlTransformationService}
     */
    protected UrlTransformationService urlTransformationService;

    public UrlTransformDataFetcher(final UrlTransformationService urlTransformationService) {
        this.urlTransformationService = urlTransformationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object doGet(final DataFetchingEnvironment environment) {
        String fieldName = environment.getField().getName();
        Map<String, Object> source = environment.getSource();
        if (source.containsKey(fieldName) && nonNull(source.get(fieldName))) {
            String transformerName = environment.getArgument(ARG_NAME_TRANSFORM);
            if (isNotEmpty(transformerName)) {
                return urlTransformationService.transform(transformerName, source.get(fieldName).toString());
            }
        }
        return source.get(fieldName);
    }

}
