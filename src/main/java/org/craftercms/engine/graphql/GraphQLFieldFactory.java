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

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Creates all the required objects to represent a content-type field in a {@link GraphQLObjectType}
 *
 * @author joseross
 * @since 3.1
 */
public interface GraphQLFieldFactory {

    /**
     * Adds all the required objects for a content-type field to a {@link GraphQLObjectType}
     *
     * @param contentTypeDefinition the XML document with the content type definition
     * @param contentTypeField      the XML node with the content-type field
     * @param contentTypeFieldId    the content-type field ID
     * @param parentGraphQLTypeName the field's parent GraphQL type name
     * @param parentGraphQLType     the field's parent {@link GraphQLObjectType}
     * @param graphQLFieldName      the field's GraphQL-friendly name
     * @param graphQLField          the field's {@link GraphQLFieldDefinition}
     */
    void createField(Document contentTypeDefinition, Node contentTypeField, String contentTypeFieldId,
                     String parentGraphQLTypeName, GraphQLObjectType.Builder parentGraphQLType,
                     String graphQLFieldName, GraphQLFieldDefinition.Builder graphQLField);

}
