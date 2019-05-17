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

package org.craftercms.engine.graphql.impl.fetchers;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.craftercms.search.elasticsearch.ElasticsearchWrapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StopWatch;

import static org.craftercms.engine.graphql.SchemaUtils.*;

/**
 * Implementation of {@link DataFetcher} that queries Elasticsearch to retrieve content based on a content type.
 * @author joseross
 * @since 3.1
 */
public class ContentTypeBasedDataFetcher implements DataFetcher<Object> {

    private static final Logger logger = LoggerFactory.getLogger(ContentTypeBasedDataFetcher.class);

    private static final String QUERY_FIELD_NAME_CONTENT_TYPE = getOriginalName(FIELD_NAME_CONTENT_TYPE);

    // Lucene regexes always match the entire string, no need to specify ^ or $
    public static final String CONTENT_TYPE_REGEX_PAGE = "/?page/.*";
    public static final String CONTENT_TYPE_REGEX_COMPONENT = "/?component/.*";

    /**
     * The default value for the 'limit' argument
     */
    protected int defaultLimit;

    /**
     * The default value for the 'sortBy' argument
     */
    protected String defaultSortField;

    /**
     * The default value for the 'sortOrder' argument
     */
    protected String defaultSortOrder;

    /**
     * The instance of {@link ElasticsearchWrapper}
     */
    protected ElasticsearchWrapper elasticsearch;

    @Required
    public void setDefaultLimit(final int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    @Required
    public void setDefaultSortField(final String defaultSortField) {
        this.defaultSortField = defaultSortField;
    }

    @Required
    public void setDefaultSortOrder(final String defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }

    @Required
    public void setElasticsearch(final ElasticsearchWrapper elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final DataFetchingEnvironment env) {
        Field field = env.getField();
        String fieldName = field.getName();

        // Get arguments for pagination & sorting
        int offset = Optional.ofNullable(env.<Integer>getArgument(ARG_NAME_OFFSET)).orElse(0);
        int limit = Optional.ofNullable(env.<Integer>getArgument(ARG_NAME_LIMIT)).orElse(defaultLimit);
        String sortBy = Optional.ofNullable(env.<String>getArgument(ARG_NAME_SORT_BY)).orElse(defaultSortField);
        String sortOrder = Optional.ofNullable(env.<String>getArgument(ARG_NAME_SORT_ORDER)).orElse(defaultSortOrder);

        List<String> queryFieldIncludes = new LinkedList<>();
        // Add content-type to includes, we might need it for a GraphQL TypeResolver
        queryFieldIncludes.add(QUERY_FIELD_NAME_CONTENT_TYPE);

        List<Map<String, Object>> items = new LinkedList<>();
        Map<String, Object> result = new HashMap<>(2);
        result.put(FIELD_NAME_ITEMS, items);

        // Setup the ES query
        SearchSourceBuilder source = new SearchSourceBuilder();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        source
            .query(query)
            .from(offset)
            .size(limit)
            .sort(sortBy, SortOrder.fromString(sortOrder));

        StopWatch watch = new StopWatch(field.getName() + " - " + field.getAlias());

        watch.start("build filters");

        // Filter by the content-type
        if (fieldName.equals(FIELD_NAME_CONTENT_ITEMS)) {
            query.filter(QueryBuilders.existsQuery(QUERY_FIELD_NAME_CONTENT_TYPE));
        } else if (fieldName.equals(FIELD_NAME_PAGES)) {
            query.filter(QueryBuilders.regexpQuery(QUERY_FIELD_NAME_CONTENT_TYPE, CONTENT_TYPE_REGEX_PAGE));
        } else if (fieldName.equals(FIELD_NAME_COMPONENTS)) {
            query.filter(QueryBuilders.regexpQuery(QUERY_FIELD_NAME_CONTENT_TYPE, CONTENT_TYPE_REGEX_COMPONENT));
        } else {
            // Get the content-type name from the field name
            query.filter(QueryBuilders.termQuery(QUERY_FIELD_NAME_CONTENT_TYPE, getOriginalName(fieldName)));
        }

        // Check the selected fields to build the ES query
        SelectedField requestedFields = env.getSelectionSet().getField(FIELD_NAME_ITEMS);
        if (Objects.nonNull(requestedFields)) {
            List<SelectedField> fields = requestedFields.getSelectionSet().getFields();
            fields.forEach(selectedField -> processSelectedField(selectedField, query, queryFieldIncludes));
        }

        // Only fetch the selected fields for better performance
        source.fetchSource(queryFieldIncludes.toArray(new String[0]), new String[0]);
        watch.stop();

        logger.debug("Executing query: {}", source);

        watch.start("searching items");
        SearchResponse response = elasticsearch.search(new SearchRequest().source(source));
        watch.stop();

        watch.start("processing items");
        result.put(FIELD_NAME_TOTAL, response.getHits().totalHits);
        if (response.getHits().totalHits > 0) {
            for(SearchHit hit :  response.getHits().getHits()) {
                items.add(fixItems(hit.getSourceAsMap()));
            }
        }
        watch.stop();

        if (logger.isTraceEnabled()) {
            logger.trace(watch.prettyPrint());
        }

        return result;
    }

    /**
     * Adds the required filters to the ES query for the given field
     */
    @SuppressWarnings("unchecked")
    protected void processSelectedField(SelectedField currentField, BoolQueryBuilder query,
                                        List<String> queryFieldIncludes)  {
        // Get the original field name
        String propertyName = getOriginalName(currentField.getQualifiedName());
        // Build the ES-friendly path
        String path = propertyName.replaceAll("/", ".");

        // Add the field to the list if it doesn't have any sub fields
        if (CollectionUtils.isEmpty(currentField.getSelectionSet().getFields())) {
            logger.debug("Adding selected field '{}' to query", path);
            queryFieldIncludes.add(path);
        }

        // Check the filters to build the ES query
        Object arg = currentField.getArguments().get(FILTER_NAME);
        if (Objects.nonNull(arg) && arg instanceof Map) {
            logger.debug("Adding filters for field {}", path);

            Map<String, Object> filters = (Map<String, Object>) arg;
            filters.forEach((name, value) -> {
                switch (name) {
                    case ARG_NAME_EQUALS:
                        query.filter(QueryBuilders.termQuery(path, value));
                        break;
                    case ARG_NAME_MATCHES:
                        query.filter(QueryBuilders.matchQuery(path, value));
                        break;
                    case ARG_NAME_REGEX:
                        query.filter(QueryBuilders.regexpQuery(path, value.toString()));
                        break;
                    case ARG_NAME_LT:
                        query.filter(QueryBuilders.rangeQuery(path).lt(value));
                        break;
                    case ARG_NAME_GT:
                        query.filter(QueryBuilders.rangeQuery(path).gt(value));
                        break;
                    case ARG_NAME_LTE:
                        query.filter(QueryBuilders.rangeQuery(path).lte(value));
                        break;
                    case ARG_NAME_GTE:
                        query.filter(QueryBuilders.rangeQuery(path).gte(value));
                        break;
                    case ARG_NAME_EXISTS:
                        boolean exists = value instanceof Boolean ? (Boolean)value : Boolean.parseBoolean(value.toString());
                        if (exists) {
                            query.filter(QueryBuilders.existsQuery(path));
                        } else {
                            query.mustNot(QueryBuilders.existsQuery(path));
                        }
                        break;
                    default:
                        // never happens
                }
            });
        }
    }

    /**
     * Checks for fields containing the 'item' keyword and makes sure they are always a list even if there is only
     * one value. This is needed because the GraphQL schema always needs to return the same type for a field.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> fixItems(Map<String, Object> map) {
        Map<String, Object> temp = new LinkedHashMap<>();

        map.forEach((key, value) -> {
            String graphQLKey = getGraphQLName(key);
            if (FIELD_NAME_ITEM.equals(key)) {
                if (!(value instanceof List)) {
                    temp.put(graphQLKey, Collections.singletonList(fixItems((Map<String, Object>)value)));
                } else {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) value;
                    temp.put(graphQLKey, list.stream().map(this::fixItems).collect(Collectors.toList()));
                }
            } else if (value instanceof Map) {
                temp.put(graphQLKey, fixItems((Map<String, Object>) value));
            } else {
                temp.put(graphQLKey, value);
            }
        });

        return MapUtils.isNotEmpty(temp)? temp : map;
    }

}
