/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.search.legacy.SiteAwareElasticsearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * REST controller to expose the Elasticsearch service
 * @author joseross
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteElasticsearchController.URL_ROOT)
public class SiteElasticsearchController extends RestControllerBase {

    public static final String URL_ROOT = "/site/elasticsearch";
    public static final String URL_SEARCH = "/search";

    protected SiteAwareElasticsearchService elasticsearchService;

    @Required
    public void setElasticsearchService(final SiteAwareElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @PostMapping(URL_SEARCH)
    public void search(@RequestBody Map<String, Object> request, @RequestParam Map<String, Object> parameters,
                       HttpServletResponse response)
            throws IOException {
        // This is needed because we are writing manually the response
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // Execute the query
        SearchResponse searchResponse = elasticsearchService.search(request, parameters);

        // Write the response in ES format
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.getWriter().write(searchResponse.toString());
    }

}
