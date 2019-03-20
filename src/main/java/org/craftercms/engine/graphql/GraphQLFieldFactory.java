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
 * @author joseross
 * @since 3.1
 */
public interface GraphQLFieldFactory {

    /**
     * Adds all the required objects for a content-type property to a {@link GraphQLObjectType}
     * @param definition the XML definition of the content-type
     * @param property the XML node of the property
     * @param fieldId the content-type property id
     * @param typeName the GraphQL-friendly name of the type
     * @param fieldName the GraphQL-friendly name of the property
     * @param newType the {@link GraphQLObjectType} for the type
     * @param newField the {@link GraphQLFieldDefinition} for the property
     */
    void createField(Document definition, Node property, String fieldId, String typeName, String fieldName,
                     GraphQLObjectType.Builder newType, GraphQLFieldDefinition.Builder newField);

}
