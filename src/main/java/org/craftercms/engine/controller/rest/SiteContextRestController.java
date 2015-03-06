package org.craftercms.engine.controller.rest;

import java.util.Collections;
import java.util.Map;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
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

    public static final String URL_ROOT =       "/site_context";
    public static final String URL_CONTEXT_ID = "/id";

    public static final String MODEL_ATTR_ID =  "id";

    @RequestMapping(value = URL_CONTEXT_ID, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getContextId() {
        SiteContext context = AbstractSiteContextResolvingFilter.getCurrentContext();
        if (context != null) {
            return Collections.singletonMap(MODEL_ATTR_ID, context.getContext().getId());
        } else {
            return Collections.emptyMap();
        }
    }

}
