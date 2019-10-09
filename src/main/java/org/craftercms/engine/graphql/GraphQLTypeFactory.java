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

import java.util.Map;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import org.craftercms.core.service.Item;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Creates all the required objects to represent a content-type in a {@link GraphQLObjectType}
 * @author joseross
 * @since 3.1
 */
public interface GraphQLTypeFactory {

    /**
     * Creates a GraphQL type for the given content-type and adds a field in the root type
     *
     * @param contentTypeDefinition the XML definition of the content-type
     * @param rootGraphQLType       the {@link GraphQLObjectType} for the root query
     * @param codeRegistry          the {@link GraphQLCodeRegistry} to add {@link graphql.schema.DataFetcher} for new
     *                              fields
     * @param siteTypes             all content-type related types
     * @param dataFetcher           the {@link DataFetcher} to use for the new fields
     */
    void createType(Item contentTypeDefinition, GraphQLObjectType.Builder rootGraphQLType,
                    GraphQLCodeRegistry.Builder codeRegistry, DataFetcher<?> dataFetcher,
                    Map<String, GraphQLObjectType.Builder> siteTypes);

    /**
     * Creates a GraphQL field for the given content-type field and adds it to the given GraphQL type
     *
     * @param contentTypeDefinition the XML definition of the content-type
     * @param contentTypeField      the XML node for the content-type field
     * @param parentGraphQLTypeName the field's parent GraphQL type name
     * @param parentGraphQLType     the field's parent {@link GraphQLObjectType}
     */
    void createField(Document contentTypeDefinition, Node contentTypeField, String parentGraphQLTypeName,
                     GraphQLObjectType.Builder parentGraphQLType);

}
