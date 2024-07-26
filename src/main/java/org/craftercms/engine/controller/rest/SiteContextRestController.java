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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.event.SiteContextCreatedEvent;
import org.craftercms.engine.event.SiteEvent;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;

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
    public static final String URL_DESTROY = "/destroy";
    public static final String URL_REBUILD = "/rebuild";
    public static final String URL_REBUILD_ALL = "/rebuild_all";
    public static final String URL_GRAPHQL = "/graphql";
    public static final String URL_STATUS = "/status";

    public static final String MODEL_ATTR_ID = "id";
    public static final String MODEL_ATTR_STATUS = "status";

    private final SiteContextManager contextManager;
    private final String configuredToken;

    @ConstructorProperties({"contextManager", "configuredToken"})
    public SiteContextRestController(final SiteContextManager contextManager, final String configuredToken) {
        this.configuredToken = configuredToken;
        this.contextManager = contextManager;
    }

    @GetMapping(value = URL_CONTEXT_ID)
    public Map<String, String> getContextId(@RequestParam String token) throws InvalidManagementTokenException {
        validateToken(token);

        return Collections.singletonMap(MODEL_ATTR_ID, SiteContext.getCurrent().getContext().getId());
    }

    @GetMapping(value = URL_DESTROY)
    public Map<String, Object> destroy(@RequestParam String token) throws InvalidManagementTokenException {
        validateToken(token);

        String siteName = SiteContext.getCurrent().getSiteName();

        contextManager.startDestroyContext(siteName);

        return createResponseMessage(format("Started destroy site context  for '%s'. " +
                "Will be recreated on next request", siteName));
    }

    @GetMapping(URL_REBUILD_ALL)
    public Map<String, Object> rebuildAll(@RequestParam String token) throws InvalidManagementTokenException {
        validateToken(token);
        contextManager.startRebuildAll();

        return createResponseMessage("Started rebuild of all site contexts");
    }

    @GetMapping(value = URL_REBUILD)
    public Map<String, Object> rebuild(HttpServletRequest request, @RequestParam String token)
            throws InvalidManagementTokenException {
        validateToken(token);

        SiteContext siteContext = SiteContext.getCurrent();
        String siteName = siteContext.getSiteName();

        // Don't rebuild context if the context was just created in this request
        if (SiteEvent.getLatestRequestEvent(SiteContextCreatedEvent.class, request) != null) {
            return createResponseMessage(format("Site context for '%s' created during the request. " +
                    "Context rebuild not necessary", siteName));
        } else {
            contextManager.startContextRebuild(siteName, siteContext.isFallback());

            return createResponseMessage(format("Started rebuild for Site context for '%s'", siteName));
        }
    }

    @GetMapping(URL_GRAPHQL + URL_REBUILD)
    public Map<String, Object> rebuildSchema(HttpServletRequest request, @RequestParam String token)
            throws InvalidManagementTokenException {
        validateToken(token);

        SiteContext siteContext = SiteContext.getCurrent();
        String siteName = siteContext.getSiteName();

        // Don't rebuild GraphQL schema if the context was just created in this request
        if (SiteEvent.getLatestRequestEvent(SiteContextCreatedEvent.class, request) != null) {
            return createResponseMessage(format("Site context for '%s' created during the request. " +
                    "GraphQL schema rebuild not necessary", siteName));
        } else {
            siteContext.startGraphQLSchemaBuild();

            return createResponseMessage(format("Rebuild of GraphQL schema started for '%s'", siteName));
        }
    }

    @GetMapping(URL_STATUS)
    public Map<String, Object> getStatus(@RequestParam String token) throws InvalidManagementTokenException {
        validateToken(token);

        return createSingletonModifiableMap(MODEL_ATTR_STATUS, SiteContext.getCurrent().getState());
    }

    protected final void validateToken(final String requestToken) throws InvalidManagementTokenException {
        if (!StringUtils.equals(requestToken, configuredToken)) {
            throw new InvalidManagementTokenException("Management authorization failed, invalid token.");
        }
    }
}
