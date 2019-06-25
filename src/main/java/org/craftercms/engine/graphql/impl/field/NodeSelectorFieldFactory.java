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
import org.apache.commons.lang3.BooleanUtils;
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
import static org.craftercms.engine.graphql.SchemaUtils.CONTENT_INCLUDE_WRAPPER_TYPE;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_COMPONENT;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_ITEM;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_KEY;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_VALUE;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SEPARATOR;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_ITEMS;
import static org.craftercms.engine.graphql.SchemaUtils.ITEM_INCLUDE_WRAPPER_TYPE;
import static org.craftercms.engine.graphql.SchemaUtils.getGraphQLName;

/**
 * Implementation of {@link GraphQLFieldFactory} that handles node-selector fields
 * @author joseross
 * @since 3.1
 */
public class NodeSelectorFieldFactory implements GraphQLFieldFactory {

    private static final Logger logger = LoggerFactory.getLogger(NodeSelectorFieldFactory.class);

    protected String disableFlatteningXPath;
    protected String datasourceNameXPath;
    protected String datasourceItemTypeXPathFormat;

    @Required
    public void setDisableFlatteningXPath(String disableFlatteningXPath) {
        this.disableFlatteningXPath = disableFlatteningXPath;
    }

    @Required
    public void setDatasourceNameXPath(final String datasourceNameXPath) {
        this.datasourceNameXPath = datasourceNameXPath;
    }

    @Required
    public void setDatasourceItemTypeXPathFormat(String datasourceItemTypeXPathFormat) {
        this.datasourceItemTypeXPathFormat = datasourceItemTypeXPathFormat;
    }

    @Override
    public void createField(final Document contentTypeDefinition, final Node contentTypeField,
                            final String contentTypeFieldId, final String parentGraphQLTypeName,
                            final GraphQLObjectType.Builder parentGraphQLType, final String graphQLFieldName,
                            final GraphQLFieldDefinition.Builder graphQLField) {
        boolean disableFlattening = BooleanUtils.toBoolean(
                XmlUtils.selectSingleNodeValue(contentTypeField, disableFlatteningXPath));

        if (disableFlattening) {
            // Flattening is disabled, so use the generic item include type
            logger.debug("Flattening is disabled for node selector '{}'. Won't generate additional schema " +
                "types and fields for its items", graphQLFieldName);

            graphQLField.type(ITEM_INCLUDE_WRAPPER_TYPE);
            return;
        }

        String datasourceName = XmlUtils.selectSingleNodeValue(contentTypeField, datasourceNameXPath);
        String itemType = XmlUtils.selectSingleNodeValue(
            contentTypeDefinition, String.format(datasourceItemTypeXPathFormat, datasourceName));
        String itemGraphQLType = StringUtils.isNotEmpty(itemType)? getGraphQLName(itemType) : null;

        if (StringUtils.isEmpty(itemGraphQLType)) {
            // If there is no item content-type set in the datasource, use the generic item include type
            logger.debug("No specific item type found for node selector '{}'. Won't generate additional schema " +
                "types and fields for its items", graphQLFieldName);

            graphQLField.type(CONTENT_INCLUDE_WRAPPER_TYPE);
        } else {
            // If there is an item content-type, then create a specific GraphQL type for it
            logger.debug("Item type found for node selector '{}': '{}'. Generating additional schema types and " +
                "fields for the items...", itemGraphQLType, graphQLFieldName);

            GraphQLObjectType flattenedType = GraphQLObjectType.newObject()
                .name(parentGraphQLTypeName + FIELD_SEPARATOR + graphQLFieldName + "_flattened_item")
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
                    .type(nonNull(GraphQLTypeReference.typeRef(itemGraphQLType))))
                .build();

            GraphQLObjectType wrapperType = GraphQLObjectType.newObject()
                .name(parentGraphQLTypeName + FIELD_SEPARATOR + graphQLFieldName + FIELD_SUFFIX_ITEMS)
                .description("Wrapper for flattened items")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_ITEM)
                    .description("List of flattened items")
                    .type(list(nonNull(flattenedType))))
                .build();

            graphQLField.type(wrapperType);
        }
    }
}
