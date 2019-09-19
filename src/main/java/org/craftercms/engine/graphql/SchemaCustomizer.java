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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.StaticDataFetcher;
import graphql.schema.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graphql.schema.FieldCoordinates.coordinates;

/**
 * Utility class used from Groovy to hold custom fields, fetchers & resolvers
 *
 * @author joseross
 * @since 3.1.4
 */
public class SchemaCustomizer {

    private static final Logger logger = LoggerFactory.getLogger(SchemaCustomizer.class);

    public static final DataFetcher<?> EMPTY_DATA_FETCHER = new StaticDataFetcher(Collections.emptyMap());

    /**
     * List of custom fields to add
     */
    protected List<FieldBuilder> fields = new LinkedList<>();

    /**
     * List of custom fetchers to add
     */
    protected List<FetcherBuilder> fetchers = new LinkedList<>();

    /**
     * List of custom resolvers to add
     */
    protected List<ResolverBuilder> resolvers = new LinkedList<>();

    /**
     * List of additional types to add
     */
    protected List<GraphQLType> additionalTypes = new LinkedList<>();

    /**
     * Adds a custom field
     * @param field the field definition
     * @param fetcher the fetcher for the field
     */
    public void field(GraphQLFieldDefinition.Builder field, DataFetcher<?> fetcher) {
        fields.add(new FieldBuilder(field, fetcher));
    }

    /**
     * Adds a custom field without a fetcher (for wrapper fields)
     * @param field the field definition
     */
    public void field(GraphQLFieldDefinition.Builder field) {
        field(field, EMPTY_DATA_FETCHER);
    }

    /**
     * Adds a custom fetcher
     * @param typeName the name of the GraphQL type
     * @param fieldName the name of the GraphQL field
     * @param dataFetcher the fetcher to use
     */
    public void fetcher(String typeName, String fieldName, DataFetcher<?> dataFetcher) {
        fetchers.add(new FetcherBuilder(typeName, fieldName, dataFetcher));
    }

    /**
     * Adds a custom resolver
     * @param typeName the name of the GraphQL interface
     * @param resolver the resolver to use
     */
    public void resolver(String typeName, TypeResolver resolver) {
        resolvers.add(new ResolverBuilder(typeName, resolver));
    }

    /**
     * Adds one or more additional types (needed during runtime but not referenced by any field)
     * @param types the types to add
     */
    public void additionalTypes(GraphQLType... types) {
        additionalTypes.addAll(Arrays.asList(types));
    }

    /**
     * Updates the root type & code registry with the custom fields & fetchers
     * @param rootTypeName the name of the root type
     * @param rootTypeBuilder the root type
     * @param codeRegistry the code registry
     */
    public void apply(String rootTypeName, GraphQLObjectType.Builder rootTypeBuilder,
                      GraphQLCodeRegistry.Builder codeRegistry) {
        fields.forEach(builder -> {
            String fieldName = builder.field.build().getName();
            logger.debug("Adding custom field & fetcher {}", fieldName);
            rootTypeBuilder.field(builder.field);
            codeRegistry.dataFetcher(coordinates(rootTypeName, fieldName), builder.fetcher);
        });
        fetchers.forEach(builder -> {
            logger.debug("Adding custom fetcher for {}/{}", builder.typeName, builder.fieldName);
            codeRegistry.dataFetcher(coordinates(builder.typeName, builder.fieldName), builder.dataFetcher);
        });
        resolvers.forEach(builder -> {
            logger.debug("Adding custom resolver for {}", builder.typeName);
            codeRegistry.typeResolver(builder.typeName, builder.resolver);
        });
    }

    /**
     * Returns the set of additional types to add
     */
    public Set<GraphQLType> getAdditionalTypes() {
        return new HashSet<>(additionalTypes);
    }

    /**
     * Utility class to hold a custom field & fetcher
     */
    private static class FieldBuilder {

        public final GraphQLFieldDefinition.Builder field;
        public final DataFetcher<?> fetcher;

        public FieldBuilder(final GraphQLFieldDefinition.Builder field, final DataFetcher<?> fetcher) {
            this.field = field;
            this.fetcher = fetcher;
        }
    }

    /**
     * Utility class to hold a custom fetcher
     */
    private static class FetcherBuilder {

        public final String typeName;
        public final String fieldName;
        public final DataFetcher<?> dataFetcher;

        public FetcherBuilder(final String typeName, final String fieldName, final DataFetcher<?> dataFetcher) {
            this.typeName = typeName;
            this.fieldName = fieldName;
            this.dataFetcher = dataFetcher;
        }
    }

    /**
     * Utility class to hold a custom resolver
     */
    private static class ResolverBuilder {

        public final String typeName;
        public final TypeResolver resolver;

        public ResolverBuilder(final String typeName, final TypeResolver resolver) {
            this.typeName = typeName;
            this.resolver = resolver;
        }
    }

}
