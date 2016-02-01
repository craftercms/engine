package org.craftercms.engine.controller.rest;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.core.service.CacheService;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST controller for operations related to a site's cache.
 *
 * @author Alfonso Vásquez
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteCacheRestController.URL_ROOT)
public class SiteCacheRestController {

    private static final Log logger = LogFactory.getLog(SiteCacheRestController.class);

    public static final String URL_ROOT = "/site/cache";
    public static final String URL_CLEAR = "/clear";

    protected CacheService cacheService;

    @Required
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @RequestMapping(value = URL_CLEAR, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> clear() {
        SiteContext siteContext = SiteContext.getCurrent();
        String siteName = siteContext.getSiteName();

        // Clear content cache
        cacheService.clearScope(siteContext.getContext());
        // Clear Freemarker cache
        siteContext.getFreeMarkerConfig().getConfiguration().clearTemplateCache();

        String msg = "Content cache and Freemarker cache have been cleared for site '" + siteName + "'";

        logger.info(msg);

        return Collections.singletonMap(RestControllerBase.MESSAGE_MODEL_ATTRIBUTE_NAME, msg);
    }

}
