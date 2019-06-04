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

package org.craftercms.engine.graphql.impl.field;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import org.craftercms.engine.graphql.GraphQLFieldFactory;
import org.dom4j.Document;
import org.dom4j.Node;

import static graphql.Scalars.GraphQLString;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_TZ;
import static org.craftercms.engine.graphql.SchemaUtils.TEXT_FILTER;
import static org.craftercms.engine.graphql.SchemaUtils.getGraphQLName;
import static org.craftercms.engine.graphql.SchemaUtils.setTypeFromFieldName;

/**
 * Implementation of {@link GraphQLFieldFactory} that handles time-only fields
 *
 * @author joseross
 * @since 3.1.1
 */
public class TimeFieldFactory implements GraphQLFieldFactory {

    @Override
    public void createField(final Document contentTypeDefinition, final Node contentTypeField,
                            final String contentTypeFieldId, final String parentGraphQLTypeName,
                            final GraphQLObjectType.Builder parentGraphQLType, final String graphQLFieldName,
                            final GraphQLFieldDefinition.Builder graphQLField) {
        // Add the timezone field as text
        parentGraphQLType.field(GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName(graphQLFieldName) + FIELD_SUFFIX_TZ)
            .description("Time Zone for field " + contentTypeFieldId)
            .type(GraphQLString)
            .argument(TEXT_FILTER));

        // Add the original according to the suffix
        setTypeFromFieldName(contentTypeFieldId, graphQLField);
    }

}
