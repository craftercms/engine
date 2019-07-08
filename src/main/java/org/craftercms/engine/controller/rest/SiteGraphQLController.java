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

package org.craftercms.engine.controller.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.graphql.QueryRequest;
import org.craftercms.engine.service.context.SiteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import static graphql.ExecutionInput.newExecutionInput;

/**
 * Exposes the current site {@link GraphQL} instance to perform queries.
 * @author joseross
 * @since 3.1
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteGraphQLController.BASE_URL)
public class SiteGraphQLController {

    private static final Logger logger = LoggerFactory.getLogger(SiteGraphQLController.class);

    public static final String BASE_URL = "/site/graphql";

    protected ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    @SuppressWarnings("unchecked")
    public Map<String, Object> query(@RequestParam String query, @RequestParam(required = false) String operationName,
                                     @RequestParam(required = false) String variablesStr) throws IOException {

        Map<String, Object> variables = StringUtils.isEmpty(variablesStr)?
            Collections.emptyMap() :
            objectMapper.readValue(variablesStr, Map.class);

        return handleRequest(query, operationName, variables);
    }

    @PostMapping
    public Map<String, Object> query(@RequestBody QueryRequest request) {
        Map<String, Object> variables = Objects.isNull(request.getVariables())?
            Collections.emptyMap() :
            request.getVariables();

        return handleRequest(request.getQuery(), request.getOperationName(), variables);
    }

    protected Map<String, Object> handleRequest(String query, String operationName, Map<String, Object> variables) {
        SiteContext siteContext = SiteContext.getCurrent();
        RequestContext requestContext = RequestContext.getCurrent();
        GraphQL graphQL = siteContext.getGraphQL();
        if (Objects.isNull(graphQL)) {
            logger.warn("GraphQL schema has not been initialized for site {}", siteContext.getSiteName());
            return Collections.singletonMap("errors",
                Collections.singletonList(
                    Collections.singletonMap("message",
                        "GraphQL schema has not been initialized for site " + siteContext.getSiteName())
                )
            );
        }

        ExecutionInput.Builder executionInput = newExecutionInput()
            .query(query)
            .operationName(operationName)
            .variables(variables)
            .context(requestContext);

        StopWatch watch = new StopWatch("graphql - " + operationName);
        watch.start("query");
        ExecutionResult result = graphQL.execute(executionInput);
        watch.stop();

        if (logger.isTraceEnabled()) {
            logger.trace(watch.prettyPrint());
        }

        return result.toSpecification();
    }

}
