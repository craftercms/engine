/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.Map;

/**
 * Implementation of {@link DataFetcher} to converts the result to {@link Map}.
 *
 * This is a workaround because the {@link graphql.schema.PropertyDataFetcher} has an internal cache for methods that
 * causes a conflict when Groovy classes are reloaded and at the moment is not possible to change the DataFetcher used.
 *
 * @author joseross
 * @since 3.1.6
 */
public class ConverterDataFetcher implements DataFetcher<Object> {

    public static ConverterDataFetcher of(DataFetcher<?> dataFetcher) {
        return new ConverterDataFetcher(dataFetcher);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    protected DataFetcher<?> dataFetcher;

    public ConverterDataFetcher(DataFetcher<?> dataFetcher) {
        this.dataFetcher = dataFetcher;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        Object source = dataFetcher.get(environment);
        if (source != null) {
            return MAPPER.convertValue(source, Object.class);
        } else {
            return null;
        }
    }

}
