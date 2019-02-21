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

package org.craftercms.engine.graphql;

import graphql.GraphQL;
import org.craftercms.engine.service.context.SiteContext;

/**
 * Creates a {@link graphql.schema.GraphQLSchema} and returns a {@link GraphQL} instance for a specific site.
 * @author joseross
 * @since 3.1
 */
public interface GraphQLFactory {

    /**
     * Returns the instance for the given {@link SiteContext}
     * @param siteContext the site context used to build the {@link graphql.schema.GraphQLSchema}
     * @return a {@link GraphQL} instance
     */
    GraphQL getInstance(SiteContext siteContext);

}
