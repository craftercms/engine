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

import java.util.*;

import graphql.schema.*;
import org.apache.commons.collections4.ListUtils;

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
 *
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
    public static final String ARG_NAME_GT = "gt";
    public static final String ARG_NAME_LTE = "lte";
    public static final String ARG_NAME_GTE = "gte";
    public static final String ARG_NAME_EXISTS = "exists";
    public static final String ARG_NAME_NOT = "not";
    public static final String ARG_NAME_OR = "or";
    public static final String ARG_NAME_AND = "and";
    public static final String ARG_NAME_TRANSFORM = "transform";

    public static final String STRING_FILTER_NAME = "StringFilters";
    public static final String TEXT_FILTER_NAME = "TextFilters";
    public static final String BOOLEAN_FILTER_NAME = "BooleanFilters";
    public static final String INT_FILTER_NAME = "IntFilters";
    public static final String FLOAT_FILTER_NAME = "FloatFilters";
    public static final String LONG_FILTER_NAME = "LongFilters";
    public static final String DATETIME_FILTER_NAME = "DateFilters";

    public static final String FIELD_SEPARATOR = "_";

    public static final String FIELD_NAME_CONTENT_ITEMS = "contentItems";
    public static final String FIELD_NAME_PAGES = "pages";
    public static final String FIELD_NAME_COMPONENTS = "components";
    public static final String FIELD_NAME_CONTENT_TYPE = getGraphQLName("content-type");
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
    public static final String FIELD_SUFFIX_TZ = FIELD_SEPARATOR + "tz";
    public static final String FIELD_SUFFIX_TOKENIZED = FIELD_SEPARATOR + "t";
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

    public static final GraphQLArgument TRANSFORM_ARG = GraphQLArgument.newArgument()
        .name(ARG_NAME_TRANSFORM)
        .description("The name of the transformer to apply")
        .type(GraphQLString)
        .build();

    public static final GraphQLArgument STRING_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name(STRING_FILTER_NAME)
            .description("Filters for 'string' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EXISTS)
                .description("Search if field exists or not (a field exists if it has a non-null value)")
                .type(GraphQLBoolean))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for exact matches")
                .type(GraphQLString))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_REGEX)
                .description("Search for a regex")
                .type(GraphQLString))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_NOT)
                .description("Combines the list of filters using the NOT operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(STRING_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_OR)
                .description("Combines the list of filters using the OR operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(STRING_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_AND)
                .description("Combines the list of filters using the AND operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(STRING_FILTER_NAME)))))
            .build())
        .build();

    public static final GraphQLArgument TEXT_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name(TEXT_FILTER_NAME)
            .description("Filters for 'text' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EXISTS)
                .description("Search if field exists or not (a field exists if it has a non-null value)")
                .type(GraphQLBoolean))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_MATCHES)
                .description("Search for terms in the text")
                .type(GraphQLString))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_REGEX)
                .description("Search for a regex")
                .type(GraphQLString))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_NOT)
                .description("Combines the list of filters using the NOT operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(TEXT_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_OR)
                .description("Combines the list of filters using the OR operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(TEXT_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_AND)
                .description("Combines the list of filters using the AND operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(TEXT_FILTER_NAME)))))
            .build())
        .build();

    public static final GraphQLArgument BOOLEAN_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name(BOOLEAN_FILTER_NAME)
            .description("Filters applicable for 'boolean' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EXISTS)
                .description("Search if field exists or not (a field exists if it has a non-null value)")
                .type(GraphQLBoolean))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for the given value")
                .type(GraphQLBoolean))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_NOT)
                .description("Combines the list of filters using the NOT operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(BOOLEAN_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_OR)
                .description("Combines the list of filters using the OR operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(BOOLEAN_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_AND)
                .description("Combines the list of filters using the AND operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(BOOLEAN_FILTER_NAME)))))
            .build())
        .build();

    public static final GraphQLArgument INT_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name(INT_FILTER_NAME)
            .description("Filters applicable for 'int' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EXISTS)
                .description("Search if field exists or not (a field exists if it has a non-null value)")
                .type(GraphQLBoolean))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for the given value")
                .type(GraphQLInt))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LT)
                .description("Search for values less than the given value")
                .type(GraphQLInt))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GT)
                .description("Search for values greater than the given value")
                .type(GraphQLInt))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GTE)
                .description("Search for values greater than or equal to the given value")
                .type(GraphQLInt))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LTE)
                .description("Search for values less than or equal to the given value")
                .type(GraphQLInt))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_NOT)
                .description("Combines the list of filters using the NOT operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(INT_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_OR)
                .description("Combines the list of filters using the OR operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(INT_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_AND)
                .description("Combines the list of filters using the AND operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(INT_FILTER_NAME)))))
            .build())
        .build();

    public static final GraphQLArgument FLOAT_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name(FLOAT_FILTER_NAME)
            .description("Filters applicable for 'float' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EXISTS)
                .description("Search if field exists or not (a field exists if it has a non-null value)")
                .type(GraphQLBoolean))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for the given value")
                .type(GraphQLFloat))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LT)
                .description("Search for values less than the given value")
                .type(GraphQLFloat))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GT)
                .description("Search for values greater than the given value")
                .type(GraphQLFloat))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GTE)
                .description("Search for values greater than or equal to the given value")
                .type(GraphQLFloat))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LTE)
                .description("Search for values less than or equal to the given value")
                .type(GraphQLFloat))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_NOT)
                .description("Combines the list of filters using the NOT operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(FLOAT_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_OR)
                .description("Combines the list of filters using the OR operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(FLOAT_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_AND)
                .description("Combines the list of filters using the AND operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(FLOAT_FILTER_NAME)))))
            .build())
        .build();

    public static final GraphQLArgument LONG_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name(LONG_FILTER_NAME)
            .description("Filters applicable for 'long' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EXISTS)
                .description("Search if field exists or not (a field exists if it has a non-null value)")
                .type(GraphQLBoolean))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EQUALS)
                .description("Search for the given value")
                .type(GraphQLLong))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LT)
                .description("Search for values less than the given value")
                .type(GraphQLLong))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GT)
                .description("Search for values greater than the given value")
                .type(GraphQLLong))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GTE)
                .description("Search for values greater than or equal to the given value")
                .type(GraphQLLong))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LTE)
                .description("Search for values less than or equal to the given value")
                .type(GraphQLLong))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_NOT)
                .description("Combines the list of filters using the NOT operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(LONG_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_OR)
                .description("Combines the list of filters using the OR operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(LONG_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_AND)
                .description("Combines the list of filters using the AND operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(LONG_FILTER_NAME)))))
            .build())
        .build();

    public static final GraphQLArgument DATETIME_FILTER = GraphQLArgument.newArgument()
        .name(FILTER_NAME)
        .description(FILTER_DESCRIPTION)
        .type(GraphQLInputObjectType.newInputObject()
            .name(DATETIME_FILTER_NAME)
            .description("Filters applicable for 'datetime' fields")
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_EXISTS)
                .description("Search if field exists or not (a field exists if it has a non-null value)")
                .type(GraphQLBoolean))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LT)
                .description("Search for values less than the given value")
                .type(DateTime))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GT)
                .description("Search for values greater than the given value")
                .type(DateTime))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_GTE)
                .description("Search for values greater than or equal to the given value")
                .type(DateTime))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_LTE)
                .description("Search for values less than or equal to the given value")
                .type(DateTime))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_NOT)
                .description("Combines the list of filters using the NOT operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(DATETIME_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_OR)
                .description("Combines the list of filters using the OR operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(DATETIME_FILTER_NAME)))))
            .field(GraphQLInputObjectField.newInputObjectField()
                .name(ARG_NAME_AND)
                .description("Combines the list of filters using the AND operator")
                .type(list(nonNull(GraphQLTypeReference.typeRef(DATETIME_FILTER_NAME)))))
            .build())
        .build();

    public static final List<GraphQLFieldDefinition> CONTENT_ITEM_FIELDS = Arrays.asList(
        GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_CONTENT_TYPE)
            .description("The content type of the item")
            .type(nonNull(GraphQLString))
            .argument(STRING_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("internal-name"))
            .description("The name/label of the item in Authoring (can also be used as a navigation label in Delivery)")
            .type(GraphQLString)
            .argument(STRING_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("localId"))
            .description("The path of the item")
            .type(GraphQLString)
            .arguments(Arrays.asList(TRANSFORM_ARG, STRING_FILTER))
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("objectId"))
            .description("The objectId of the item")
            .type(nonNull(GraphQLString))
            .argument(STRING_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("objectGroupId"))
            .description("The objectGroupId for the item")
            .type(nonNull(GraphQLString))
            .argument(STRING_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("createdDate_dt"))
            .description("The created date of the item")
            .type(nonNull(DateTime))
            .argument(DATETIME_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("lastModifiedDate_dt"))
            .description("The last modified date of the item")
            .type(nonNull(DateTime))
            .argument(DATETIME_FILTER)
            .build()
    );

    public static final List<GraphQLFieldDefinition> PAGE_FIELDS = Arrays.asList(
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("placeInNav"))
            .description("If the page should be placed in the navigation")
            .type(GraphQLBoolean)
            .argument(BOOLEAN_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("orderDefault_f"))
            .description("The order the page has in the navigation")
            .type(GraphQLFloat)
            .argument(FLOAT_FILTER)
            .build(),
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getGraphQLName("navLabel"))
            .description("The label of the page in the navigation")
            .type(GraphQLString)
            .argument(STRING_FILTER)
            .build()
    );

    public static final GraphQLInterfaceType CONTENT_ITEM_INTERFACE_TYPE = GraphQLInterfaceType.newInterface()
        .name("ContentItem")
        .description("Interface for all content items (pages, components and taxonomies)")
        .fields(CONTENT_ITEM_FIELDS)
        .build();

    public static final GraphQLInterfaceType PAGE_INTERFACE_TYPE = GraphQLInterfaceType.newInterface()
        .name("Page")
        .description("Interface for pages")
        .fields(ListUtils.union(CONTENT_ITEM_FIELDS, PAGE_FIELDS))
        .build();

    public static final GraphQLObjectType ITEM_INCLUDE_TYPE = GraphQLObjectType.newObject()
        .name("ItemInclude")
        .description("Holds a reference to another item in the site")
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_VALUE)
            .description("The name of the item")
            .type(nonNull(GraphQLString))
            .argument(TEXT_FILTER))
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_KEY)
            .description("The path of the item")
            .type(nonNull(GraphQLString))
            .argument(TEXT_FILTER))
        .build();

    public static final GraphQLObjectType ITEM_INCLUDE_WRAPPER_TYPE = GraphQLObjectType.newObject()
        .name("ItemIncludeWrapper")
        .description("Wrapper for a list of item references")
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_ITEM)
            .description("List of item references")
            .type(list(ITEM_INCLUDE_TYPE)))
        .build();

    public static final GraphQLObjectType CONTENT_INCLUDE_TYPE = GraphQLObjectType.newObject()
        .name("ContentInclude")
        .description("Holds the content of another item in the site")
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_VALUE)
            .description("The name of the item")
            .type(nonNull(GraphQLString))
            .argument(TEXT_FILTER))
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_KEY)
            .description("The path of the item")
            .type(nonNull(GraphQLString))
            .argument(TEXT_FILTER))
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_COMPONENT)
            .description("The content of the item")
            .type(nonNull(CONTENT_ITEM_INTERFACE_TYPE)))
        .build();

    public static final GraphQLObjectType CONTENT_INCLUDE_WRAPPER_TYPE = GraphQLObjectType.newObject()
        .name("ContentIncludeWrapper")
        .description("Wrapper for a list of flattened components")
        .field(GraphQLFieldDefinition.newFieldDefinition()
            .name(FIELD_NAME_ITEM)
            .description("List of items")
            .type(list(CONTENT_INCLUDE_TYPE)))
        .build();

    public static final TypeResolver CONTENT_TYPE_BASED_TYPE_RESOLVER = env -> {
        Object item = env.getObject();
        if (item instanceof Map) {
            Object contentType = ((Map) item).get(FIELD_NAME_CONTENT_TYPE);
            if (contentType != null) {
                return env.getSchema().getObjectType(getGraphQLName(contentType.toString()));
            }
        }

        return null;
    };

    // Utility methods

    /**
     * Returns a GraphQL-friendly name
     */
    public static String getGraphQLName(String name) {
        return name
            .replaceAll("-", "__")
            .replaceAll("^/page/", "page_")
            .replaceAll("^/component/", "component_")
            .replaceAll("^/taxonomy", "taxonomy")
            .replaceAll("/", "___");
    }

    /**
     * Returns the original name from a GraphQL-friendly one
     */
    public static String getOriginalName(String graphQLName) {
        return graphQLName
            .replaceAll("^page_", "/page/")
            .replaceAll("^component_", "/component/")
            .replaceAll("^taxonomy", "/taxonomy")
            .replaceAll("___", "/")
            .replaceAll("__", "-");
    }

    /**
     * Tries to set the type of a field based on its name suffix
     */
    public static void setTypeFromFieldName(String fieldName, GraphQLFieldDefinition.Builder field) {
        if (fieldName.endsWith("_s")) {
            field.type(GraphQLString);
            field.argument(STRING_FILTER);
        } else if (fieldName.endsWith("_dt") || fieldName.endsWith("_to")) {
            field.type(DateTime);
            field.argument(DATETIME_FILTER);
        } else if (fieldName.endsWith("_b")) {
            field.type(GraphQLBoolean);
            field.argument(BOOLEAN_FILTER);
        } else if (fieldName.endsWith("_i")) {
            field.type(GraphQLInt);
            field.argument(INT_FILTER);
        } else if (fieldName.endsWith("_f") || fieldName.endsWith("_d")) {
            // GraphQL Float is actually a Java Double
            field.type(GraphQLFloat);
            field.argument(FLOAT_FILTER);
        } else if (fieldName.endsWith("_l")) {
            field.type(GraphQLLong);
            field.argument(LONG_FILTER);
        } else {
            field.type(GraphQLString);
            field.argument(TEXT_FILTER);
        }
    }

    /**
     * Creates a query wrapper type (with total and list of items) using a reference to a type
     */
    public static GraphQLType createQueryWrapperType(String namePrefix, String description) {
        return GraphQLObjectType.newObject()
            .name(namePrefix + FIELD_SUFFIX_QUERY)
            .description(description)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_TOTAL)
                .description("Total number of items found")
                .type(nonNull(GraphQLInt)))
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_ITEMS)
                .description("List of items")
                .type(list(nonNull(GraphQLTypeReference.typeRef(namePrefix)))))
            .build();
    }

    /**
     * Creates a query wrapper type (with total and list of items) for an actual type
     */
    public static GraphQLType createQueryWrapperType(String namePrefix, GraphQLType wrappedType,
                                                     String description) {
        return GraphQLObjectType.newObject()
            .name(namePrefix + FIELD_SUFFIX_QUERY)
            .description(description)
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_TOTAL)
                .description("Total number of items found")
                .type(nonNull(GraphQLInt)))
            .field(GraphQLFieldDefinition.newFieldDefinition()
                .name(FIELD_NAME_ITEMS)
                .description("List of items")
                .type(list(nonNull(wrappedType))))
            .build();
    }

}
