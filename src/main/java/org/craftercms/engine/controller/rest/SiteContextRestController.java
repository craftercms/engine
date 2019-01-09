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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST controller for operations related for the {@link org.craftercms.engine.service.context.SiteContext}
 *
 * @author Alfonso Vásquez
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteContextRestController.URL_ROOT)
public class SiteContextRestController {

    public static final String URL_ROOT = "/site/context";
    public static final String URL_CONTEXT_ID = "/id";
    public static final String URL_DESTROY = "/destroy";
    public static final String URL_REBUILD = "/rebuild";

    public static final String MODEL_ATTR_ID =  "id";

    private SiteContextManager contextRegistry;

    @Required
    public void setContextRegistry(SiteContextManager contextRegistry) {
        this.contextRegistry = contextRegistry;
    }

    @RequestMapping(value = URL_CONTEXT_ID, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getContextId() {
        return Collections.singletonMap(MODEL_ATTR_ID, SiteContext.getCurrent().getContext().getId());
    }

    @RequestMapping(value = URL_DESTROY, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> destroy() {
        String siteName = SiteContext.getCurrent().getSiteName();

        contextRegistry.destroyContext(siteName);

        return Collections.singletonMap(RestControllerBase.MESSAGE_MODEL_ATTRIBUTE_NAME,
                                        "Site context for '" + siteName + "' destroyed. Will be recreated on next " +
                                        "request");
    }

    @RequestMapping(value = URL_REBUILD, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> rebuild() {
        Lock lock = contextRegistry.getLock();
        SiteContext siteContext = SiteContext.getCurrent();
        String siteName = siteContext.getSiteName();
        boolean fallback = siteContext.isFallback();

        lock.lock();
        try {
            contextRegistry.destroyContext(siteName);

            siteContext = contextRegistry.getContext(siteName, fallback);
            SiteContext.setCurrent(siteContext);

            return Collections.singletonMap(RestControllerBase.MESSAGE_MODEL_ATTRIBUTE_NAME, "Site context for '" +
                                                                                             siteName + "' rebuilt");
        } finally {
            lock.unlock();
        }
    }

}
