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
import graphql.schema.GraphQLTypeReference;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.engine.graphql.GraphQLFieldFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_COMPONENT;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_ITEM;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_KEY;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_VALUE;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SEPARATOR;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_ITEMS;
import static org.craftercms.engine.graphql.SchemaUtils.INCLUDE_WRAPPER_TYPE;
import static org.craftercms.engine.graphql.SchemaUtils.getGraphQLName;

/**
 * Implementation of {@link GraphQLFieldFactory} that handles node-selector fields
 * @author joseross
 * @since 3.1
 */
public class NodeSelectorFieldFactory implements GraphQLFieldFactory {

    private static final Logger logger = LoggerFactory.getLogger(NodeSelectorFieldFactory.class);

    protected String contentTypePropertyItemManagerXpath;

    @Required
    public void setContentTypePropertyItemManagerXpath(final String contentTypePropertyItemManagerXpath) {
        this.contentTypePropertyItemManagerXpath = contentTypePropertyItemManagerXpath;
    }

    @Override
    public void createField(final Document definition, final Node property, final String fieldId,
                            final String typeName, final String fieldName, final GraphQLObjectType.Builder newType,
                            final GraphQLFieldDefinition.Builder newField) {
        String datasourceName = XmlUtils.selectSingleNodeValue(property, contentTypePropertyItemManagerXpath);
        String datasourceType = XmlUtils.selectSingleNodeValue(definition,
            "form/datasources/datasource/id[text()='" + datasourceName + "']/../properties/property/name[text" +
            "()='type']/../value");
        String referencedType = StringUtils.isNotEmpty(datasourceType)? getGraphQLName(datasourceType) : null;
        if (StringUtils.isEmpty(referencedType)) {
            // If there is no content-type set in the datasource, just add a generic type for includes
            logger.debug("Empty type for datasource '{}' in field '{}'", datasourceName, fieldName);
            newField.type(INCLUDE_WRAPPER_TYPE);
        } else {
            // If there is a content-type, then create a specific type for it
            logger.debug("Adding reference to type '{}' for field '{}'", referencedType, fieldName);
            GraphQLObjectType flattenedType = GraphQLObjectType.newObject()
                .name(typeName + FIELD_SEPARATOR + fieldName + "_flattened_item")
                .description("Contains the data from another item in the site")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_VALUE)
                    .description("The name of the item")
                    .type(nonNull(GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_KEY)
                    .description("The path of the item")
                    .type(nonNull(GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_COMPONENT)
                    .description("The content of the item")
                    .type(nonNull(GraphQLTypeReference.typeRef(referencedType))))
                .build();

            GraphQLObjectType wrapperType = GraphQLObjectType.newObject()
                .name(typeName + FIELD_SEPARATOR + fieldName + FIELD_SUFFIX_ITEMS)
                .description("Wrapper for flattened items")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_ITEM)
                    .description("List of flattened items")
                    .type(list(nonNull(flattenedType))))
                .build();

            newField.type(wrapperType);
        }
    }
}
