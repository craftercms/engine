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

package org.craftercms.engine.graphql.impl.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.craftercms.commons.http.RequestContext;

/**
 * Base implementation for {@link DataFetcher} that set's the current {@link RequestContext} before actually
 * resolving the requested fields.
 *
 * @since 3.1.1
 * @author joseross
 */
public abstract class RequestAwareDataFetcher<T> implements DataFetcher<T> {

    @Override
    public T get(final DataFetchingEnvironment environment) throws Exception {
        try {
            RequestContext.setCurrent(environment.getContext());
            return doGet(environment);
        } finally {
            RequestContext.clear();
        }
    }

    /**
     * Performs the actual fetching of the requested fields.
     * @param environment the {@link DataFetchingEnvironment}
     * @return the resolved value
     * @throws Exception if there is any error resolving the requested field
     */
    public abstract T doGet(final DataFetchingEnvironment environment) throws Exception;

}
