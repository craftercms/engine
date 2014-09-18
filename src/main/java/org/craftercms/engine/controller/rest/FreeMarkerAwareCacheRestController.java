package org.craftercms.engine.controller.rest;

import java.util.Collection;
import java.util.Map;

import org.craftercms.core.controller.rest.CacheRestController;
import org.craftercms.core.exception.CacheException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextRegistry;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Extension of {@link org.craftercms.core.controller.rest.CacheRestController} that adds the functionality of
 * clearing FreeMarker caches.
 *
 * @author avasquez
 */
public class FreeMarkerAwareCacheRestController extends CacheRestController {

    protected SiteContextRegistry siteContextRegistry;

    @Required
    public void setSiteContextRegistry(final SiteContextRegistry siteContextRegistry) {
        this.siteContextRegistry = siteContextRegistry;
    }

    @Override
    public Map<String, String> clearAllScopes() throws CacheException {
        Collection<SiteContext> contexts = siteContextRegistry.list();

        for (SiteContext context : contexts) {
            context.getFreeMarkerConfig().getConfiguration().clearTemplateCache();
        }

        return super.clearAllScopes();
    }

    @Override
    public Map<String, String> clearScope(@RequestParam(CacheRestController.REQUEST_PARAM_CONTEXT_ID) String contextId)
        throws InvalidContextException, CacheException {
        Collection<SiteContext> contexts = siteContextRegistry.list();

        for (SiteContext context : contexts) {
            if (context.getContext().getId().equals(contextId)) {
                context.getFreeMarkerConfig().getConfiguration().clearTemplateCache();
                break;
            }
        }

        return super.clearScope(contextId);
    }

}
