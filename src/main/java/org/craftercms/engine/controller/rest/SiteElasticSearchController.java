/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All Rights Reserved.
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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.search.SiteAwareElasticSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * REST controller to expose the ElasticSearch service
 * @author joseross
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteElasticSearchController.URL_ROOT)
public class SiteElasticSearchController {

    public static final String URL_ROOT = "/site/elasticsearch";
    public static final String URL_SEARCH = "/search";

    protected SiteAwareElasticSearchService elasticSearchService;

    @Required
    public void setElasticSearchService(final SiteAwareElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    @RequestMapping(value = URL_SEARCH, method = {GET, POST})
    public SearchResponse search(HttpServletRequest request) throws Exception {
        try(InputStream is = request.getInputStream()) {
            String json = IOUtils.toString(is, Charset.defaultCharset());
            SearchModule module = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
            SearchSourceBuilder builder =
                SearchSourceBuilder
                    .fromXContent(XContentFactory.xContent(XContentType.JSON)
                        .createParser(new NamedXContentRegistry(module.getNamedXContents()),
                            DeprecationHandler.THROW_UNSUPPORTED_OPERATION, json));
            return elasticSearchService.search(new SearchRequest().source(builder));
        }
    }

}
