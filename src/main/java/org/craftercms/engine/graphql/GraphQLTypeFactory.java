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
     * @param formDefinition the XML definition of the content-type
     * @param rootType  the {@link GraphQLObjectType} for the root query
     * @param codeRegistry  the {@link GraphQLCodeRegistry} to add {@link graphql.schema.DataFetcher} for new fields
     */
    void createType(Item formDefinition, GraphQLObjectType.Builder rootType, GraphQLCodeRegistry.Builder codeRegistry);

    /**
     * Creates a GraphQL field for the given content-type property and adds it to the given type
     * @param formDefinition the XML definition of the content-type
     * @param typeName the GraphQL-friendly name for the type
     * @param property the XML node for the property
     * @param newType the {@link GraphQLObjectType} for the type
     */
    void createField(Document formDefinition, String typeName, Node property, GraphQLObjectType.Builder newType);

}
