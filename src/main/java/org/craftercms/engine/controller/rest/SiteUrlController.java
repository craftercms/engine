package org.craftercms.engine.controller.rest;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.UrlTransformationService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteUrlController.URL_ROOT)
public class SiteUrlController extends RestControllerBase {

    public static final String URL_ROOT = "/site/url";
    public static final String URL_TRANSFORM = "/transform";

    protected UrlTransformationService urlTransformationService;

    @Required
    public void setUrlTransformationService(final UrlTransformationService urlTransformationService) {
        this.urlTransformationService = urlTransformationService;
    }

    @GetMapping(URL_TRANSFORM)
    public String transformUrl(@RequestParam String transformerName, @RequestParam String url) {
        return urlTransformationService.transform(transformerName, url);
    }

}
