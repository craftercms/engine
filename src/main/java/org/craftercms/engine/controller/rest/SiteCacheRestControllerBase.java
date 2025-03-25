/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.core.cache.CacheStatistics;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.core.service.CacheService;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.beans.ConstructorProperties;
import java.util.Map;

import static java.lang.String.format;

/**
 * Base class for site-related cache controllers.
 *
 * @author avasquez
 */
public class SiteCacheRestControllerBase extends RestControllerBase {

    private static final Log logger = LogFactory.getLog(SiteCacheRestControllerBase.class);

    public static final String URL_CLEAR = "/clear";
    public static final String URL_STATS = "/statistics";

    protected CacheService cacheService;
    protected final String configuredToken;

    @ConstructorProperties({"cacheService", "configuredToken"})
    public SiteCacheRestControllerBase(final CacheService cacheService, final String configuredToken) {
        this.cacheService = cacheService;
        this.configuredToken = configuredToken;
    }

    @RequestMapping(value = URL_CLEAR, method = RequestMethod.GET)
    public Map<String, Object> clear(HttpServletRequest request, @RequestParam String token) throws InvalidManagementTokenException {
        validateToken(token);

        SiteContext siteContext = SiteContext.getCurrent();
        String siteName = siteContext.getSiteName();

        cacheService.clearScope(siteContext.getContext());

        String msg = format("Cache clear for site '%s' completed", siteName);

        logger.debug(msg);

        return createResponseMessage(msg);
    }

    @RequestMapping(value = URL_STATS, method = RequestMethod.GET)
    public CacheStatistics getStatistics(@RequestParam String token) throws InvalidManagementTokenException {
        validateToken(token);

        return cacheService.getStatistics(SiteContext.getCurrent().getContext());
    }

    protected final void validateToken(final String requestToken) throws InvalidManagementTokenException {
        if (!StringUtils.equals(requestToken, configuredToken)) {
            throw new InvalidManagementTokenException("Management authorization failed, invalid token.");
        }
    }

}
