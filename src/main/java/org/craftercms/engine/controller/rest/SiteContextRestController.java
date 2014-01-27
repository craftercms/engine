package org.craftercms.engine.controller.rest;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller for stuff related for the {@link org.craftercms.engine.service.context.SiteContext}
 *
 * @author Alfonso Vásquez
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteContextRestController.URL_ROOT)
public class SiteContextRestController {

    public static final String URL_ROOT =       "/site_context";
    public static final String URL_CONTEXT_ID = "/id";

    public static final String MODEL_ATTR_SITE_NAME =   "siteName";
    public static final String MODEL_ATTR_ID =          "id";


    @RequestMapping(value = URL_CONTEXT_ID, method = RequestMethod.GET)
    public Map<String, Object> getContextId() {
        SiteContext context = AbstractSiteContextResolvingFilter.getCurrentContext();
        if (context != null) {
            Map<String, Object> model = new LinkedHashMap<String, Object>(2);
            model.put(MODEL_ATTR_SITE_NAME, context.getSiteName());
            model.put(MODEL_ATTR_ID, context.getContext().getId());

            return model;
        } else {
            return Collections.emptyMap();
        }
    }

}
