package org.craftercms.engine.controller.rest;

import java.util.Collections;
import java.util.Map;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextRegistry;
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

    public static final String URL_ROOT = "/site_context";
    public static final String URL_CONTEXT_ID = "/id";
    public static final String URL_FORCE_RECREATE = "/force_recreate";

    public static final String MODEL_ATTR_ID =  "id";

    private SiteContextRegistry contextRegistry;

    @Required
    public void setContextRegistry(SiteContextRegistry contextRegistry) {
        this.contextRegistry = contextRegistry;
    }

    @RequestMapping(value = URL_CONTEXT_ID, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> getContextId() {
        return Collections.singletonMap(MODEL_ATTR_ID, SiteContext.getCurrent().getContext().getId());
    }

    @RequestMapping(value = URL_FORCE_RECREATE, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> forceContextRecreate() {
        String siteName = SiteContext.getCurrent().getSiteName();

        contextRegistry.unregister(siteName);

        return Collections.singletonMap(RestControllerBase.MESSAGE_MODEL_ATTRIBUTE_NAME,
                                        "Site context for '" + siteName + " destroyed. Will be recreated on next " +
                                        "request");
    }

}
