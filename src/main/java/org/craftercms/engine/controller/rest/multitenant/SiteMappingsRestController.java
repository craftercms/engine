package org.craftercms.engine.controller.rest.multitenant;

import java.util.Collections;
import java.util.Map;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.context.ReloadableMappingsSiteResolver;
import org.craftercms.engine.service.context.SiteResolver;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST controller for operations related to site mappings.
 *
 * @author avasquez
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteMappingsRestController.URL_ROOT)
public class SiteMappingsRestController {

    public static final String URL_ROOT = "/site/mappings";
    public static final String URL_RELOAD = "/reload";

    private SiteResolver siteResolver;

    @Required
    public void setSiteResolver(SiteResolver siteResolver) {
        this.siteResolver = siteResolver;
    }

    @RequestMapping(value = URL_RELOAD, method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> reloadMappings() {
        if (siteResolver instanceof ReloadableMappingsSiteResolver) {
            ((ReloadableMappingsSiteResolver)siteResolver).reloadMappings();

            return Collections.singletonMap(RestControllerBase.MESSAGE_MODEL_ATTRIBUTE_NAME, "Mappings reloaded");
        } else{
            return Collections.singletonMap(RestControllerBase.MESSAGE_MODEL_ATTRIBUTE_NAME,
                                            "The current resolver is not a " +
                                            ReloadableMappingsSiteResolver.class.getSimpleName() +
                                            ". No mappings to reload");
        }
    }

}
