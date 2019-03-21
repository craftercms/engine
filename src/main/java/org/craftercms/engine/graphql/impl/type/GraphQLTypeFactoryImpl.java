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

package org.craftercms.engine.graphql.impl.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.core.service.Item;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.engine.graphql.GraphQLFieldFactory;
import org.craftercms.engine.graphql.GraphQLTypeFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static graphql.Scalars.GraphQLInt;
import static graphql.schema.AsyncDataFetcher.async;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static org.craftercms.engine.graphql.SchemaUtils.COMMON_ITEM_FIELDS;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_ITEMS;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_NAME_TOTAL;
import static org.craftercms.engine.graphql.SchemaUtils.FIELD_SUFFIX_QUERY;
import static org.craftercms.engine.graphql.SchemaUtils.TYPE_ARGUMENTS;
import static org.craftercms.engine.graphql.SchemaUtils.getGraphQLName;
import static org.craftercms.engine.graphql.SchemaUtils.setTypeFromFieldName;

/**
 * @author joseross
 */
public class GraphQLTypeFactoryImpl implements GraphQLTypeFactory {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLTypeFactoryImpl.class);

    /**
     * The name for the root Query type
     */
    protected String rootQueryTypeName;

    /**
     * The list of fields that should not be added to the {@link GraphQLSchema}
     */
    protected String[] ignoredFields;

    /**
     * XPath selectors for the content-type definition file
     */
    protected String contentTypeNameXpath;
    protected String contentTypeTitleXpath;
    protected String contentTypeFieldsXpath;
    protected String contentTypePropertyIdXpath;
    protected String contentTypePropertyTypeXpath;
    protected String contentTypePropertyTitleXpath;

    /**
     * All known field factories to use during type build
     */
    protected Map<String, GraphQLFieldFactory> fieldFactories;

    /**
     * The {@link DataFetcher} to use for queries
     */
    protected DataFetcher<Object> dataFetcher;

    @Required
    public void setRootQueryTypeName(final String rootQueryTypeName) {
        this.rootQueryTypeName = rootQueryTypeName;
    }

    public void setIgnoredFields(final String[] ignoredFields) {
        this.ignoredFields = ignoredFields;
    }

    @Required
    public void setContentTypeNameXpath(final String contentTypeNameXpath) {
        this.contentTypeNameXpath = contentTypeNameXpath;
    }

    @Required
    public void setContentTypeTitleXpath(final String contentTypeTitleXpath) {
        this.contentTypeTitleXpath = contentTypeTitleXpath;
    }

    @Required
    public void setContentTypeFieldsXpath(final String contentTypeFieldsXpath) {
        this.contentTypeFieldsXpath = contentTypeFieldsXpath;
    }

    @Required
    public void setContentTypePropertyIdXpath(final String contentTypePropertyIdXpath) {
        this.contentTypePropertyIdXpath = contentTypePropertyIdXpath;
    }

    @Required
    public void setContentTypePropertyTypeXpath(final String contentTypePropertyTypeXpath) {
        this.contentTypePropertyTypeXpath = contentTypePropertyTypeXpath;
    }

    @Required
    public void setContentTypePropertyTitleXpath(final String contentTypePropertyTitleXpath) {
        this.contentTypePropertyTitleXpath = contentTypePropertyTitleXpath;
    }

    @Required
    public void setFieldFactories(final Map<String, GraphQLFieldFactory> fieldFactories) {
        this.fieldFactories = fieldFactories;
    }

    @Required
    public void setDataFetcher(final DataFetcher<Object> dataFetcher) {
        this.dataFetcher = dataFetcher;
    }

    /**
     * {@inheritDoc}
     */
    public void createType(Item formDefinition, GraphQLObjectType.Builder rootType,
                              GraphQLCodeRegistry.Builder codeRegistry) {
        logger.debug("Creating GraphQL Type from '{}'", formDefinition.getUrl());

        Document definition = formDefinition.getDescriptorDom();
        String contentTypeName = XmlUtils.selectSingleNodeValue(definition, contentTypeNameXpath);
        String typeName = getGraphQLName(contentTypeName);
        logger.debug("Creating GraphQL Type '{}' for '{}'", typeName, contentTypeName);

        GraphQLObjectType.Builder newType = GraphQLObjectType.newObject()
            .name(typeName)
            .description(XmlUtils.selectSingleNodeValue(definition, contentTypeTitleXpath));

        List<Node> properties = XmlUtils.selectNodes(definition, contentTypeFieldsXpath, Collections.emptyMap());

        // Add commons fields
        newType.fields(COMMON_ITEM_FIELDS);

        // Add the content-type specific fields
        if (CollectionUtils.isNotEmpty(properties)) {
            for(Node property : properties) {
                createField(definition, typeName, property, newType);
            }
        }

        // Create a wrapper type for the queries of the content-type
        GraphQLObjectType queryType = GraphQLObjectType.newObject()
            .name(typeName + FIELD_SUFFIX_QUERY)
            .description("Query for content-type " + contentTypeName)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_TOTAL)
                .description("Total number of items found")
                .type(nonNull(GraphQLInt)))
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_ITEMS)
                .description("List of items for " + contentTypeName)
                .type(list(nonNull(newType.build()))))
            .build();

        // Add a field in the root type
        rootType.field(GraphQLFieldDefinition.newFieldDefinition()
            .name(typeName)
            .description("Query descriptors for content type " + contentTypeName)
            .type(nonNull(queryType))
            .arguments(TYPE_ARGUMENTS)
        );

        // Add the data fetcher for the new field
        codeRegistry.dataFetcher(FieldCoordinates.coordinates(rootQueryTypeName, typeName), async(dataFetcher));

    }

    /**
     * {@inheritDoc}
     */
    public void createField(Document formDefinition, String typeName, Node property,
                            GraphQLObjectType.Builder newType) {
        String fieldId = XmlUtils.selectSingleNodeValue(property, contentTypePropertyIdXpath);

        if (ArrayUtils.isNotEmpty(ignoredFields) && ArrayUtils.contains(ignoredFields, fieldId)) {
            return;
        }

        String type = XmlUtils.selectSingleNodeValue(property, contentTypePropertyTypeXpath);
        String fieldName = getGraphQLName(fieldId);
        logger.debug("Creating GraphQL field '{}' for '{}'", fieldName, fieldId);

        GraphQLFieldDefinition.Builder newField = GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName(fieldName))
            .description(XmlUtils.selectSingleNodeValue(property, contentTypePropertyTitleXpath));

        if (fieldFactories.containsKey(type)) {
            fieldFactories.get(type).createField(formDefinition, property, fieldId, typeName, fieldName, newType, newField);
        } else {
            setTypeFromFieldName(fieldName, newField);
        }

        newType.field(newField);
    }

}
