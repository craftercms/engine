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
import java.util.List;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.scalars.ExtendedScalars.DateTime;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;

/**
 * Utility objects & methods for building the GraphQL Schema
 * @author joseross
 * @since 3.1
 */
public abstract class SchemaUtils {

    // Constants

    public static final String ARG_NAME_SORT_BY = "sortBy";

    public static final String ARG_NAME_SORT_ORDER = "sortOrder";

    public static final String ARG_NAME_OFFSET = "offset";

    public static final String ARG_NAME_LIMIT = "limit";

    public static final String ARG_NAME_EQUALS = "equals";

    public static final String ARG_NAME_MATCHES = "matches";

    public static final String ARG_NAME_REGEX = "regex";

    public static final String ARG_NAME_LT = "lt";

    public static final String ARG_NAME_GT = "GT";

    public static final String ARG_NAME_BEFORE = "before";

    public static final String ARG_NAME_AFTER = "after";

    public static final String FIELD_SEPARATOR = "_";

    public static final String FIELD_NAME_ITEM = "item";

    public static final String FIELD_NAME_ITEMS = "items";

    public static final String FIELD_NAME_TOTAL = "total";

    public static final String FIELD_NAME_KEY = "key";

    public static final String FIELD_NAME_VALUE = "value";

    public static final String FIELD_NAME_SELECTED = "selected";

    public static final String FIELD_NAME_COMPONENT = "component";

    public static final String FIELD_SUFFIX_ITEM = FIELD_SEPARATOR + FIELD_NAME_ITEM;

    public static final String FIELD_SUFFIX_ITEMS = FIELD_SEPARATOR + FIELD_NAME_ITEMS;

    public static final String FIELD_SUFFIX_QUERY = FIELD_SEPARATOR + "query";

    public static final String FIELD_SUFFIX_RAW = FIELD_SEPARATOR + "raw";

    public static final String FIELD_SUFFIX_MULTIVALUE  = "mv";

    public static final String FILTER_NAME = "filter";

    public static final String FILTER_DESCRIPTION = "Values used to filter the results";

    // Base Types that apply for all sites

    public static final GraphQLEnumType ORDER_ENUM = GraphQLEnumType.newEnum()
        .name("SortOrder")
        .description("Possible values for sorting")
        .value("ASC")
        .value("DESC")
        .build();

    public static final List<GraphQLArgument> TYPE_ARGUMENTS = Arrays.asList(
        GraphQLArgument.newArgument()
            .name(ARG_NAME_SORT_BY)
            .description("The name of the field to sort items")
            .type(GraphQLString)
            .build(),
        GraphQLArgument.newArgument()
            .name(ARG_NAME_SORT_ORDER)
            .description("The order to sort items")
            .type(ORDER_ENUM)
            .build(),
        GraphQLArgument.newArgument()
            .name(ARG_NAME_OFFSET)
            .description("The number of items to skip")
            .type(GraphQLInt)
            .build(),
        GraphQLArgument.newArgument()
            .name(ARG_NAME_LIMIT)
            .description("The number of items to return")
            .type(GraphQLInt)
            .build()
    );

    public static final GraphQLArgument STRING_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name("StringFilters")
            .description("Filters for 'string' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for exact matches")
                .type(GraphQLString))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_REGEX)
                .description("Search for a regex")
                .type(GraphQLString))
            .build())
        .build();

    public static final GraphQLArgument TEXT_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name("TextFilters")
            .description("Filters for 'text' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_MATCHES)
                .description("Search for terms in the text")
                .type(GraphQLString))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_REGEX)
                .description("Search for a regex")
                .type(GraphQLString))
            .build())
        .build();

    public static final GraphQLArgument BOOLEAN_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name("BooleanFilters")
            .description("Filters applicable for 'boolean' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for the given value")
                .type(GraphQLBoolean))
            .build())
        .build();

    public static final GraphQLArgument INT_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name("IntFilters")
            .description("Filters applicable for 'int' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for the given value")
                .type(GraphQLInt))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LT)
                .description("Search for values lower than the given value")
                .type(GraphQLInt))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GT)
                .description("Search for values greater than the given value")
                .type(GraphQLInt))
            .build())
        .build();

    public static final GraphQLArgument FLOAT_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name("FloatFilters")
            .description("Filters applicable for 'float' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for the given value")
                .type(GraphQLFloat))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LT)
                .description("Search for values lower than the given value")
                .type(GraphQLFloat))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GT)
                .description("Search for values greater than the given value")
                .type(GraphQLFloat))
            .build())
        .build();

    public static final GraphQLArgument LONG_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name("LongFilters")
            .description("Filters applicable for 'long' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for the given value")
                .type(GraphQLLong))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LT)
                .description("Search for values lower than the given value")
                .type(GraphQLLong))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GT)
                .description("Search for values greater than the given value")
                .type(GraphQLLong))
            .build())
        .build();

    public static final GraphQLArgument DATETIME_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name("DateFilters")
            .description("Filters applicable for 'datetime' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_BEFORE)
                .description("Search for values before the given value")
                .type(DateTime))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_AFTER)
                .description("Search for values after the given value")
                .type(DateTime))
            .build())
        .build();

    public static final List<GraphQLFieldDefinition> DESCRIPTOR_FIELDS = Arrays.asList(
        GraphQLFieldDefinition.newFieldDefinition()
            .name("localId")
            .description("The unique path for the descriptor")
            .type(nonNull(GraphQLString))
            .argument(STRING_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name("objectId")
            .description("The unique objectId for the descriptor")
            .type(nonNull(GraphQLString))
            .argument(STRING_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name("objectGroupId")
            .description("The objectGroupId for the descriptor")
            .type(nonNull(GraphQLString))
            .argument(STRING_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name("createdDate_dt")
            .description("The creation date for the descriptor")
            .type(nonNull(DateTime))
            .argument(DATETIME_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name("lastModifiedDate_dt")
            .description("The last modified date for the descriptor")
            .type(nonNull(DateTime))
            .argument(DATETIME_FILTER)
            .build()
    );

    public static final GraphQLObjectType INCLUDE_TYPE = GraphQLObjectType.newObject()
        .name("DescriptorInclude")
        .description("Holds a reference to another descriptor in the site")
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_VALUE)
            .description("Indicates the descriptor name")
            .type(nonNull(GraphQLString))
            .argument(TEXT_FILTER))
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_KEY)
            .description("Indicates the descriptor path")
            .type(nonNull(GraphQLString))
            .argument(TEXT_FILTER))
        .build();

    public static final GraphQLObjectType INCLUDE_WRAPPER_TYPE = GraphQLObjectType.newObject()
        .name("DescriptorIncludeWrapper")
        .description("Wrapper for a list of descriptor references")
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_ITEM)
            .description("List of descriptor references")
            .type(list(INCLUDE_TYPE)))
        .build();

    // Utility methods

    /**
     * Returns a GraphQL-friendly name
     */
    public static String getGraphQLName(String name) {
        return name
            .replaceAll("-", "__")
            .replaceAll("^/page/", "page_")
            .replaceAll("^/component/", "component_")
            .replaceAll("^/taxonomy", "taxonomy");
    }

    /**
     * Returns the original name from a GraphQL-friendly one
     */
    public static String getOriginalName(String graphQLName) {
        return graphQLName
            .replaceAll("^page_", "/page/")
            .replaceAll("^component_", "/component/")
            .replaceAll("^taxonomy", "/taxonomy")
            .replaceAll("__", "-");
    }

}
