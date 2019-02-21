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

package org.craftercms.engine.graphql.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.Tree;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.engine.graphql.GraphQLFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StopWatch;
import com.fasterxml.jackson.databind.ObjectMapper;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.scalars.ExtendedScalars.DateTime;
import static graphql.schema.AsyncDataFetcher.async;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static org.craftercms.engine.graphql.SchemaUtils.*;

/**
 * Default implementation of {@link GraphQLFactory} that creates a {@link GraphQLSchema} from the content-type
 * definitions found in the site repository
 * @author joseross
 * @since 3.1
 */
public class GraphQLFactoryImpl implements GraphQLFactory {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLFactory.class);

    /**
     * The path to look for content-type definitions
     */
    protected String repoConfigFolder;

    /**
     * The name of the file containing the content-type definition
     */
    protected String contentTypeDefinitionName;

    /**
     * The name for the root Query type
     */
    protected String rootQueryTypeName;

    /**
     * The {@link DataFetcher} to use for queries
     */
    protected DataFetcher<Object> dataFetcher;

    /**
     * The list of fields that should not be added to the {@link GraphQLSchema}
     */
    protected String[] ignoredFields;

    /**
     * Object mapper used to parse JSON values from the content-type definitions
     */
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * XPath selectors for the content-type definition file
     */
    protected String contentTypeNameXpath;
    protected String contentTypeTitleXpath;
    protected String contentTypeFieldsXpath;
    protected String contentTypePropertyIdXpath;
    protected String contentTypePropertyTypeXpath;
    protected String contentTypePropertyTitleXpath;
    protected String contentTypePropertyDataSourceNameXpath;
    protected String contentTypePropertyItemManagerXpath;
    protected String contentTypePropertyFieldsXpath;

    @Required
    public void setRepoConfigFolder(final String repoConfigFolder) {
        this.repoConfigFolder = repoConfigFolder;
    }

    @Required
    public void setRootQueryTypeName(final String rootQueryTypeName) {
        this.rootQueryTypeName = rootQueryTypeName;
    }

    @Required
    public void setContentTypeDefinitionName(final String contentTypeDefinitionName) {
        this.contentTypeDefinitionName = contentTypeDefinitionName;
    }

    @Required
    public void setDataFetcher(final DataFetcher<Object> dataFetcher) {
        this.dataFetcher = dataFetcher;
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
    public void setContentTypePropertyDataSourceNameXpath(final String contentTypePropertyDataSourceNameXpath) {
        this.contentTypePropertyDataSourceNameXpath = contentTypePropertyDataSourceNameXpath;
    }

    @Required
    public void setContentTypePropertyItemManagerXpath(final String contentTypePropertyItemManagerXpath) {
        this.contentTypePropertyItemManagerXpath = contentTypePropertyItemManagerXpath;
    }

    @Required
    public void setContentTypePropertyFieldsXpath(final String contentTypePropertyFieldsXpath) {
        this.contentTypePropertyFieldsXpath = contentTypePropertyFieldsXpath;
    }

    /**
     * Recursively looks for content-type definitions
     */
    protected void findContentTypes(Tree item, GraphQLObjectType.Builder rootType,
                                    GraphQLCodeRegistry.Builder codeRegistry) {
        logger.debug("Looking for content-type definitions in '{}'", item.getUrl());
        List<Item> children = item.getChildren();
        if(CollectionUtils.isNotEmpty(children)) {
            Optional<Item> formDefinition = children.stream()
                .filter(i -> contentTypeDefinitionName.equals(i.getName()))
                .findFirst();
            if(formDefinition.isPresent()) {
                createType(formDefinition.get(), rootType, codeRegistry);
            } else {
                children.forEach(i -> findContentTypes((Tree) i, rootType, codeRegistry));
            }
        }
    }

    /**
     * Creates a GraphQL type for the given content-type and adds a field in the root type
     */
    protected void createType(Item formDefinition, GraphQLObjectType.Builder rootType,
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
        newType.fields(DESCRIPTOR_FIELDS);

        // Add the content-type specific fields
        if(CollectionUtils.isNotEmpty(properties)) {
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
     * Creates a GraphQL field for the given content-type property
     */
    protected void createField(Document definition, String typeName, Node property,
                               GraphQLObjectType.Builder newType) {
        String fieldId = XmlUtils.selectSingleNodeValue(property, contentTypePropertyIdXpath);

        if(ArrayUtils.isNotEmpty(ignoredFields) && ArrayUtils.contains(ignoredFields, fieldId)) {
            return;
        }

        String type = XmlUtils.selectSingleNodeValue(property, contentTypePropertyTypeXpath);
        String fieldName = getGraphQLName(fieldId);
        logger.debug("Creating GraphQL field '{}' for '{}'", fieldName, fieldId);

        GraphQLFieldDefinition.Builder newField = GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName(fieldName))
            .description(XmlUtils.selectSingleNodeValue(property, contentTypePropertyTypeXpath));

        switch (type) {
            case "input":
            case "datetime":
            case "image-picker":
            case "video-picker":
            case "date-time":
            case "dropdown":
                guessTypeFromName(fieldName, newField);
                break;
            case "checkbox":
                newField.type(GraphQLBoolean);
                newField.argument(BOOLEAN_FILTER);
                break;
            case "checkbox-group":
                createCheckboxGroupField(definition, property, fieldId, typeName, fieldName, newField);
                break;
            case "node-selector":
                createNodeSelectorField(definition, property, typeName, fieldName, newField);
                break;
            case "repeat":
                createRepeatingGroupField(definition, property, fieldId, typeName, fieldName, newField);
                break;
            case "rte":
            case "rte-tinymce4":
            case "rte-tinymce5":
                // For RTEs add the copy field (with no filters to avoid searches with markup)
                newType.field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(getGraphQLName(fieldName) + FIELD_SUFFIX_RAW)
                    .description(XmlUtils.selectSingleNodeValue(property, contentTypePropertyTitleXpath))
                    .type(GraphQLString));
                newField.type(GraphQLString);
                newField.argument(TEXT_FILTER);
                break;
            default:
                // for any other field just add it as text (but this should not happen)
                newField.type(GraphQLString);
                newField.argument(TEXT_FILTER);
        }

        newType.field(newField);
    }

    /**
     * Creates the required wrapper types for a checkbox-group property
     */
    @SuppressWarnings("unchecked")
    protected void createCheckboxGroupField(Document definition, Node property, String fieldId, String typeName,
                                            String fieldName, GraphQLFieldDefinition.Builder newField) {
        String dataSourceName = XmlUtils.selectSingleNodeValue(property, contentTypePropertyDataSourceNameXpath);
        String dataSourceSettings = XmlUtils.selectSingleNodeValue(definition,
            "form/datasources/datasource/id[text()='" + dataSourceName + "']/../properties/property/name[text"
                + "()='dataType']/../value");
        String dataSourceType = null;
        String dataSourceSuffix = null;
        try {
            List<Map<String, Object>> typeSetting = objectMapper.readValue(dataSourceSettings, List.class);
            Optional<Map<String, Object>> selectedType = typeSetting.stream()
                                                            .filter(s -> (Boolean) s.get(FIELD_NAME_SELECTED))
                                                            .findFirst();
            if(selectedType.isPresent()) {
                dataSourceType = selectedType.get().get(FIELD_NAME_VALUE).toString();
                dataSourceSuffix = StringUtils.substringAfter(dataSourceType, FIELD_SEPARATOR);
            }
        } catch (IOException e) {
            logger.warn("Error checking data source type for {}", fieldId);
        }
        String valueKey = FIELD_NAME_VALUE;
        if(StringUtils.isNotEmpty(dataSourceSuffix)) {
            valueKey += FIELD_SEPARATOR + dataSourceSuffix + FIELD_SUFFIX_MULTIVALUE;
        }

        GraphQLFieldDefinition.Builder valueField = GraphQLFieldDefinition.newFieldDefinition()
            .name(valueKey)
            .description("The value for the item");

        guessTypeFromName(dataSourceType, valueField);

        GraphQLObjectType itemType = GraphQLObjectType.newObject()
            .name(typeName + FIELD_SEPARATOR + fieldName + FIELD_SUFFIX_ITEM)
            .description("Item for field " + fieldId)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_KEY)
                .description("The key for the item")
                .type(GraphQLString)
                .argument(TEXT_FILTER))
            .field(valueField)
            .build();

        GraphQLObjectType itemWrapper = GraphQLObjectType.newObject()
            .name(typeName + FIELD_SEPARATOR + fieldName + FIELD_SUFFIX_ITEMS)
            .description("Wrapper for field " + fieldId)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_ITEM)
                .description("List of items for field " + fieldId)
                .type(list(itemType)))
            .build();

        newField.type(itemWrapper);
    }

    /**
     * Creates the required wrapper types for a node-selector property
     */
    protected void createNodeSelectorField(Document definition, Node property, String typeName,
                                           String fieldName, GraphQLFieldDefinition.Builder newField) {
        String dataSourceName = XmlUtils.selectSingleNodeValue(property, contentTypePropertyItemManagerXpath);
        String dataSourceType = XmlUtils.selectSingleNodeValue(definition,
            "form/datasources/datasource/id[text()='" + dataSourceName + "']/../properties/property/name[text"
                + "()='type']/../value");
        if(StringUtils.isEmpty(dataSourceType)) {
            // If there is no content-type set in the datasource, just add a generic type for includes
            newField.type(INCLUDE_WRAPPER_TYPE);
        } else {
            // If there is a content-type, then create a specific type for it
            GraphQLObjectType flattenedType = GraphQLObjectType.newObject()
                .name(typeName + FIELD_SEPARATOR + fieldName + "_flattened_item")
                .description("Holds the data from another descriptor in the site")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_VALUE)
                    .description("Indicates the descriptor name")
                    .type(nonNull(GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_KEY)
                    .description("Indicates the descriptor path")
                    .type(nonNull(GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_COMPONENT)
                    .description("The descriptor data")
                    .type(nonNull(GraphQLTypeReference.typeRef(getGraphQLName(dataSourceType)))))
                .build();

            GraphQLObjectType wrapperType = GraphQLObjectType.newObject()
                .name(typeName + FIELD_SEPARATOR + fieldName + FIELD_SUFFIX_ITEMS)
                .description("Wrapper for child descriptors")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(FIELD_NAME_ITEM)
                    .description("List of child descriptors")
                    .type(list(nonNull(flattenedType))))
                .build();

            newField.type(wrapperType);
        }
    }

    /**
     * Creates the required wrapper types for a repeat property
     */
    protected void createRepeatingGroupField(Document definition, Node property, String fieldId, String typeName,
                                             String fieldName, GraphQLFieldDefinition.Builder newField) {
        // For Repeating Groups we need to create a wrapper type and do everything all over
        GraphQLObjectType.Builder repeatType = GraphQLObjectType.newObject()
            .name(typeName + FIELD_SEPARATOR + fieldName + FIELD_SUFFIX_ITEM)
            .description("Item for repeat group of " + fieldId);

        List<Node> fields =
            XmlUtils.selectNodes(property, contentTypePropertyFieldsXpath, Collections.emptyMap());

        // Call recursively for all fields in the repeating group
        if(CollectionUtils.isNotEmpty(fields)) {
            fields.forEach(f -> createField(definition, typeName, f, repeatType));
        }

        GraphQLObjectType wrapperType = GraphQLObjectType.newObject()
            .name(typeName + FIELD_SEPARATOR + fieldName + FIELD_SUFFIX_ITEMS)
            .description("Wrapper for list of items of " + fieldId)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_ITEM)
                .description("List of items of " + fieldId)
                .type(list(repeatType.build())))
            .build();

        newField.type(wrapperType);
    }

    /**
     * Tries to set the type of a field based on its name suffix
     */
    protected void guessTypeFromName(String fieldName, GraphQLFieldDefinition.Builder newField) {
        if(fieldName.endsWith("_s")) {
            newField.type(GraphQLString);
            newField.argument(STRING_FILTER);
        } else if(fieldName.endsWith("_dt")) {
            newField.type(DateTime);
            newField.argument(DATETIME_FILTER);
        } else if(fieldName.endsWith("_b")) {
            newField.type(GraphQLBoolean);
            newField.argument(BOOLEAN_FILTER);
        } else if(fieldName.endsWith("_i")) {
            newField.type(GraphQLInt);
            newField.argument(INT_FILTER);
        } else if(fieldName.endsWith("_f") || fieldName.endsWith("_d")) {
            // GraphQL Float is actually a Java Double
            newField.type(GraphQLFloat);
            newField.argument(FLOAT_FILTER);
        } else if(fieldName.endsWith("_l")) {
            newField.type(GraphQLLong);
            newField.argument(LONG_FILTER);
        } else {
            newField.type(GraphQLString);
            newField.argument(TEXT_FILTER);
        }
    }

    /**
     * Creates the root Query type and looks for all existing content-type definitions
     * @param siteContext the site context
     * @return the {@link GraphQLSchema} instance
     */
    protected GraphQLSchema buildSchema(SiteContext siteContext) {
        logger.debug("Building GraphQL Schema for site '{}'", siteContext.getSiteName());

        GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();

        GraphQLObjectType.Builder rootType = GraphQLObjectType.newObject()
            .name(rootQueryTypeName)
            .description("Provides access to all site content");

        ContentStoreService storeService = siteContext.getStoreService();
        Tree tree = storeService.findTree(siteContext.getContext(), repoConfigFolder);
        if(Objects.nonNull(tree)) {
            findContentTypes(tree, rootType, codeRegistry);
        }

        return GraphQLSchema.newSchema()
            .codeRegistry(codeRegistry.build())
            .query(rootType)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    public GraphQL getInstance(SiteContext siteContext) {
        StopWatch watch = new StopWatch("GraphQL Schema");
        watch.start("Schema Build");
        GraphQLSchema schema = buildSchema(siteContext);
        watch.stop();
        if(logger.isTraceEnabled()) {
            logger.trace(watch.prettyPrint());
        }
        return GraphQL.newGraphQL(schema).build();
    }

}
