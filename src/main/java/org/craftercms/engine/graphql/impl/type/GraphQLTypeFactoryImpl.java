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

import graphql.schema.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static graphql.schema.FieldCoordinates.coordinates;
import static graphql.schema.GraphQLNonNull.nonNull;
import static org.craftercms.engine.graphql.SchemaUtils.*;

/**
 * @author joseross
 */
public class GraphQLTypeFactoryImpl implements GraphQLTypeFactory {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLTypeFactoryImpl.class);

    public static final String CONTENT_TYPE_REGEX_PAGE = "^/?page/.*$";

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
    protected String contentTypeNameXPath;
    protected String contentTypeTitleXPath;
    protected String contentTypeFieldsXPath;
    protected String contentTypeFieldIdXPath;
    protected String contentTypeFieldTypeXPath;
    protected String contentTypeFieldTitleXPath;

    /**
     * All known field factories to use during type build
     */
    protected Map<String, GraphQLFieldFactory> fieldFactories;

    /**
     * Custom {@link DataFetcher}s to use for specific fields
     */
    protected Map<String, DataFetcher> customFetchers;

    @Required
    public void setCustomFetchers(final Map<String, DataFetcher> customFetchers) {
        this.customFetchers = customFetchers;
    }

    @Required
    public void setRootQueryTypeName(final String rootQueryTypeName) {
        this.rootQueryTypeName = rootQueryTypeName;
    }

    public void setIgnoredFields(final String[] ignoredFields) {
        this.ignoredFields = ignoredFields;
    }

    @Required
    public void setContentTypeNameXPath(final String contentTypeNameXPath) {
        this.contentTypeNameXPath = contentTypeNameXPath;
    }

    @Required
    public void setContentTypeTitleXPath(final String contentTypeTitleXPath) {
        this.contentTypeTitleXPath = contentTypeTitleXPath;
    }

    @Required
    public void setContentTypeFieldsXPath(final String contentTypeFieldsXPath) {
        this.contentTypeFieldsXPath = contentTypeFieldsXPath;
    }

    @Required
    public void setContentTypeFieldIdXPath(final String contentTypeFieldIdXPath) {
        this.contentTypeFieldIdXPath = contentTypeFieldIdXPath;
    }

    @Required
    public void setContentTypeFieldTypeXPath(final String contentTypeFieldTypeXPath) {
        this.contentTypeFieldTypeXPath = contentTypeFieldTypeXPath;
    }

    @Required
    public void setContentTypeFieldTitleXPath(final String contentTypeFieldTitleXPath) {
        this.contentTypeFieldTitleXPath = contentTypeFieldTitleXPath;
    }

    @Required
    public void setFieldFactories(final Map<String, GraphQLFieldFactory> fieldFactories) {
        this.fieldFactories = fieldFactories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createType(Item formDefinition, GraphQLObjectType.Builder rootGraphQLType,
                           GraphQLCodeRegistry.Builder codeRegistry, DataFetcher<?> dataFetcher,
                           Map<String, GraphQLObjectType.Builder> siteTypes) {
        logger.debug("Creating GraphQL Type from '{}'", formDefinition.getUrl());

        Document contentTypeDefinition = formDefinition.getDescriptorDom();
        String contentTypeName = XmlUtils.selectSingleNodeValue(contentTypeDefinition, contentTypeNameXPath);
        String graphQLTypeName = getGraphQLName(contentTypeName);

        logger.debug("Creating GraphQL Type '{}' for '{}'", graphQLTypeName, contentTypeName);

        GraphQLObjectType.Builder graphQLType = GraphQLObjectType.newObject()
            .withInterface(CONTENT_ITEM_INTERFACE_TYPE)
            .name(graphQLTypeName)
            .description(XmlUtils.selectSingleNodeValue(contentTypeDefinition, contentTypeTitleXPath));

        // Add commons fields
        graphQLType.fields(CONTENT_ITEM_FIELDS);

        if (contentTypeName.matches(CONTENT_TYPE_REGEX_PAGE)) {
            graphQLType.withInterface(PAGE_INTERFACE_TYPE);
            graphQLType.fields(PAGE_FIELDS);
        }

        // Add the type builder so it's available later to the customizer
        siteTypes.put(graphQLTypeName, graphQLType);

        List<Node> contentTypeFields = XmlUtils.selectNodes(contentTypeDefinition, contentTypeFieldsXPath,
                                                            Collections.emptyMap());
        // Add the content-type specific fields
        if (CollectionUtils.isNotEmpty(contentTypeFields)) {
            for(Node contentTypeField : contentTypeFields) {
                createField(contentTypeDefinition, contentTypeField, graphQLTypeName, graphQLType);
            }
        }

        // Create a wrapper type for the queries of the content-type
        GraphQLType queryType = createQueryWrapperType(graphQLTypeName, "Query for content-type " + contentTypeName);

        // Add a field in the root type
        rootGraphQLType.field(GraphQLFieldDefinition.newFieldDefinition()
                                                    .name(graphQLTypeName)
                                                    .description("Items of content-type " + contentTypeName)
                                                    .type(nonNull(queryType))
                                                    .arguments(TYPE_ARGUMENTS)
                             );

        // Add the data fetcher for the new field
        codeRegistry.dataFetcher(coordinates(rootQueryTypeName, graphQLTypeName), dataFetcher);

        // Add the custom data fetchers for fields
        if (MapUtils.isNotEmpty(customFetchers)) {
            customFetchers.forEach((fieldName, customFetcher) -> {
                String graphQLFieldName = getGraphQLName(fieldName);
                if (graphQLType.hasField(graphQLFieldName)) {
                    codeRegistry.dataFetcher(coordinates(graphQLTypeName, graphQLFieldName), customFetcher);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createField(Document contentTypeDefinition, Node contentTypeField, String parentGraphQLTypeName,
                            GraphQLObjectType.Builder parentGraphQLType) {
        String contentTypeFieldId = XmlUtils.selectSingleNodeValue(contentTypeField, contentTypeFieldIdXPath);

        if (ArrayUtils.isNotEmpty(ignoredFields) && ArrayUtils.contains(ignoredFields, contentTypeFieldId)) {
            return;
        }

        String contentTypeFieldType = XmlUtils.selectSingleNodeValue(contentTypeField, contentTypeFieldTypeXPath);
        String graphQLFieldName = getGraphQLName(contentTypeFieldId);

        // Don't add the field again if it already exists
        if (!parentGraphQLType.hasField(graphQLFieldName)) {
            logger.debug("Creating GraphQL field '{}' for '{}'", graphQLFieldName, contentTypeFieldId);

            GraphQLFieldDefinition.Builder graphQLField = GraphQLFieldDefinition.newFieldDefinition()
                .name(graphQLFieldName)
                .description(XmlUtils.selectSingleNodeValue(contentTypeField, contentTypeFieldTitleXPath));

            if (fieldFactories.containsKey(contentTypeFieldType)) {
                fieldFactories.get(contentTypeFieldType).createField(contentTypeDefinition, contentTypeField,
                                                                     contentTypeFieldId, parentGraphQLTypeName,
                                                                     parentGraphQLType, graphQLFieldName, graphQLField);
            } else {
                setTypeFromFieldName(contentTypeFieldId, graphQLField);
            }

            parentGraphQLType.field(graphQLField);
        }
    }

}
