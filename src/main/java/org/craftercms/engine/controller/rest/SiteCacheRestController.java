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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.cache.CacheStatistics;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.event.SiteContextCreatedEvent;
import org.craftercms.engine.event.SiteEvent;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * REST controller for operations related to a site's cache.
 *
 * @author Alfonso Vásquez
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteCacheRestController.URL_ROOT)
public class SiteCacheRestController extends RestControllerBase {

    private static final Log logger = LogFactory.getLog(SiteCacheRestController.class);

    public static final String URL_ROOT = "/site/cache";
    public static final String URL_CLEAR = "/clear";
    public static final String URL_STATS = "/statistics";

    @RequestMapping(value = URL_CLEAR, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> clear(HttpServletRequest request) {
        SiteContext siteContext = SiteContext.getCurrent();
        String siteName = siteContext.getSiteName();
        String msg;

        // Don't clear cache if the context was just created in this request
        if (SiteEvent.getLatestRequestEvent(SiteContextCreatedEvent.class, request) != null) {
            return createResponseMessage("Site context for '" + siteName + "' created during the request. " +
                                         "Cache clear not necessary");
        } else {
            siteContext.startCacheClear();

            msg = "Cache clear for site '" + siteName + "' started";
        }

        logger.debug(msg);

        return createResponseMessage(msg);
    }

    @ResponseBody
    @RequestMapping(value = URL_STATS, method = RequestMethod.GET)
    public CacheStatistics getStatistics() {
        SiteContext siteContext = SiteContext.getCurrent();

        return siteContext.getCacheService().getStatistics(siteContext.getContext());
    }

}
