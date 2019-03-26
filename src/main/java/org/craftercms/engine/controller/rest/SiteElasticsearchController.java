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

import java.util.Map;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.search.SiteAwareElasticsearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to expose the Elasticsearch service
 * @author joseross
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteElasticsearchController.URL_ROOT)
public class SiteElasticsearchController {

    public static final String URL_ROOT = "/site/elasticsearch";
    public static final String URL_SEARCH = "/search";

    protected SiteAwareElasticsearchService elasticsearchService;

    @Required
    public void setElasticsearchService(final SiteAwareElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @PostMapping(URL_SEARCH)
    public SearchResponse search(@RequestBody Map<String, Object> request) {
        return elasticsearchService.search(request);
    }

}
