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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import graphql.GraphQL;
import graphql.schema.*;
import org.apache.commons.collections.CollectionUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.Tree;
import org.craftercms.engine.graphql.GraphQLFactory;
import org.craftercms.engine.graphql.GraphQLTypeFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StopWatch;

import static graphql.schema.AsyncDataFetcher.async;
import static graphql.schema.FieldCoordinates.coordinates;
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
     * The {@link GraphQLTypeFactory} to use for all content-types
     */
    protected GraphQLTypeFactory typeFactory;

    /**
     * The {@link DataFetcher} to use for queries
     */
    protected DataFetcher<?> dataFetcher;

    @Required
    public void setRepoConfigFolder(final String repoConfigFolder) {
        this.repoConfigFolder = repoConfigFolder;
    }

    @Required
    public void setContentTypeDefinitionName(final String contentTypeDefinitionName) {
        this.contentTypeDefinitionName = contentTypeDefinitionName;
    }

    @Required
    public void setRootQueryTypeName(final String rootQueryTypeName) {
        this.rootQueryTypeName = rootQueryTypeName;
    }

    @Required
    public void setTypeFactory(final GraphQLTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Required
    public void setDataFetcher(DataFetcher<?> dataFetcher) {
        this.dataFetcher = async(dataFetcher);
    }

    /**
     * Recursively looks for content-type definitions
     */
    protected void findContentTypes(Tree item, GraphQLObjectType.Builder rootType,
                                    GraphQLCodeRegistry.Builder codeRegistry) {
        logger.debug("Looking for content-type definitions in '{}'", item.getUrl());
        List<Item> children = item.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            Optional<Item> formDefinition = children.stream()
                .filter(i -> contentTypeDefinitionName.equals(i.getName()))
                .findFirst();
            if (formDefinition.isPresent()) {
                typeFactory.createType(formDefinition.get(), rootType, codeRegistry);
            } else {
                children.stream()
                    .filter(i -> i instanceof Tree)
                    .forEach(i -> findContentTypes((Tree) i, rootType, codeRegistry));
            }
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

        // Add the type resolver for the interfaces
        codeRegistry.typeResolver(CONTENT_ITEM_INTERFACE_TYPE, CONTENT_TYPE_BASED_TYPE_RESOLVER);
        codeRegistry.typeResolver(PAGE_INTERFACE_TYPE, CONTENT_TYPE_BASED_TYPE_RESOLVER);

        // Add the all items field to the root type
        rootType.field(GraphQLFieldDefinition.newFieldDefinition()
             .name(FIELD_NAME_CONTENT_ITEMS)
             .description("All content items")
             .type(nonNull(createQueryWrapperType(FIELD_NAME_CONTENT_ITEMS, CONTENT_ITEM_INTERFACE_TYPE,
                                                  "Query for all content items")))
             .arguments(TYPE_ARGUMENTS)
        );

        // Add the all pages field to the root type
        rootType.field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_PAGES)
            .description("All pages")
            .type(nonNull(createQueryWrapperType(FIELD_NAME_PAGES, PAGE_INTERFACE_TYPE,
                                                 "Query for all pages")))
            .arguments(TYPE_ARGUMENTS)
        );

        // Add the all components field to the root type
        rootType.field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_COMPONENTS)
            .description("All components")
            .type(nonNull(createQueryWrapperType(FIELD_NAME_COMPONENTS, CONTENT_ITEM_INTERFACE_TYPE,
                                                 "Query for all components")))
            .arguments(TYPE_ARGUMENTS)
        );

        // Add the data fetcher for the new fields
        codeRegistry.dataFetcher(coordinates(rootQueryTypeName, FIELD_NAME_CONTENT_ITEMS), dataFetcher);
        codeRegistry.dataFetcher(coordinates(rootQueryTypeName, FIELD_NAME_PAGES), dataFetcher);
        codeRegistry.dataFetcher(coordinates(rootQueryTypeName, FIELD_NAME_COMPONENTS), dataFetcher);

        ContentStoreService storeService = siteContext.getStoreService();
        Tree tree = storeService.findTree(siteContext.getContext(), repoConfigFolder);
        if (Objects.nonNull(tree)) {
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
        if (logger.isTraceEnabled()) {
            logger.trace(watch.prettyPrint());
        }

        return GraphQL.newGraphQL(schema).build();
    }

}
