/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.core.controller.rest.CrafterRestController;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.core.service.CacheService;
import org.springframework.web.bind.annotation.RequestMapping;

import java.beans.ConstructorProperties;

/**
 * REST controller for operations related to a site's application cache.
 *
 * @author avasquez
 */
@CrafterRestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteAppCacheRestController.URL_ROOT)
public class SiteAppCacheRestController extends SiteCacheRestControllerBase {

    public static final String URL_ROOT = "/site/app_cache";

    @ConstructorProperties({"cacheService", "configuredToken"})
    public SiteAppCacheRestController(final CacheService cacheService, final String configuredToken) {
        super(cacheService, configuredToken);
    }

}
