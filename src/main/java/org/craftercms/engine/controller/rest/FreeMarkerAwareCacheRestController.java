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

import java.util.Collection;
import java.util.Map;

import org.craftercms.core.controller.rest.CacheRestController;
import org.craftercms.core.exception.CacheException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Extension of {@link org.craftercms.core.controller.rest.CacheRestController} that adds the functionality of
 * clearing FreeMarker caches.
 *
 * @author avasquez
 */
public class FreeMarkerAwareCacheRestController extends CacheRestController {

    protected SiteContextManager siteContextManager;

    @Required
    public void setSiteContextManager(final SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    @Override
    public Map<String, String> clearAllScopes() throws CacheException {
        Collection<SiteContext> contexts = siteContextManager.listContexts();

        for (SiteContext siteContext : contexts) {
            siteContext.getFreeMarkerConfig().getConfiguration().clearTemplateCache();
        }

        return super.clearAllScopes();
    }

    @Override
    public Map<String, String> clearScope(@RequestParam(CacheRestController.REQUEST_PARAM_CONTEXT_ID) String contextId)
        throws InvalidContextException, CacheException {
        Collection<SiteContext> contexts = siteContextManager.listContexts();

        for (SiteContext siteContext : contexts) {
            if (siteContext.getContext().getId().equals(contextId)) {
                siteContext.getFreeMarkerConfig().getConfiguration().clearTemplateCache();
                break;
            }
        }

        return super.clearScope(contextId);
    }

}
