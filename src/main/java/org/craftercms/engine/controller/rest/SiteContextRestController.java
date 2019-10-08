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

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.event.SiteContextCreatedEvent;
import org.craftercms.engine.event.SiteEvent;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * REST controller for operations related for the {@link org.craftercms.engine.service.context.SiteContext}
 *
 * @author Alfonso Vásquez
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteContextRestController.URL_ROOT)
public class SiteContextRestController extends RestControllerBase {

    public static final String URL_ROOT = "/site/context";
    public static final String URL_CONTEXT_ID = "/id";
    public static final String URL_LATEST_EVENTS = "/latest_events";
    public static final String URL_DESTROY = "/destroy";
    public static final String URL_REBUILD = "/rebuild";
    public static final String URL_GRAPHQL = "/graphql";

    public static final String MODEL_ATTR_ID =  "id";

    private SiteContextManager contextManager;

    @Required
    public void setContextManager(SiteContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @GetMapping(value = URL_CONTEXT_ID)
    public Map<String, String> getContextId() {
        return Collections.singletonMap(MODEL_ATTR_ID, SiteContext.getCurrent().getContext().getId());
    }

    @GetMapping(value = URL_DESTROY)
    public Map<String, Object> destroy() {
        String siteName = SiteContext.getCurrent().getSiteName();

        contextManager.destroyContext(siteName);

        return createResponseMessage("Site context for '" + siteName + "' destroyed. Will be recreated on next " +
                                     "request");
    }

    @GetMapping(value = URL_REBUILD)
    public Map<String, Object> rebuild(HttpServletRequest request) {
        SiteContext siteContext = SiteContext.getCurrent();
        String siteName = siteContext.getSiteName();

        // Don't rebuild context if the context was just created in this request
        if (SiteEvent.getLatestRequestEvent(SiteContextCreatedEvent.class, request) != null) {
            return createResponseMessage("Site context for '" + siteName + "' created during the request. " +
                                         "Context rebuild not necessary");
        } else {
            contextManager.startContextRebuild(siteName, siteContext.isFallback());

            return createResponseMessage("Started rebuild for Site context for '" + siteName + "'");
        }
    }

    @GetMapping(URL_GRAPHQL + URL_REBUILD)
    public Map<String, Object> rebuildSchema(HttpServletRequest request) {
        SiteContext siteContext = SiteContext.getCurrent();
        String siteName = siteContext.getSiteName();

        // Don't rebuild GraphQL schema if the context was just created in this request
        if (SiteEvent.getLatestRequestEvent(SiteContextCreatedEvent.class, request) != null) {
            return createResponseMessage("Site context for '" + siteName + "' created during the request. " +
                                         "GraphQL schema rebuild not necessary");
        } else {
            siteContext.startGraphQLSchemaBuild();

            return createResponseMessage("Rebuild of GraphQL schema for started for '" + siteName + "'");
        }
    }

}
