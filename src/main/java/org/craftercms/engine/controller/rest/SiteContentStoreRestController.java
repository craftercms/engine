
package org.craftercms.engine.controller.rest;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.core.controller.rest.ContentStoreRestController;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import static org.craftercms.core.controller.rest.ContentStoreRestController.REQUEST_PARAM_TREE_DEPTH;
import static org.craftercms.core.controller.rest.ContentStoreRestController.REQUEST_PARAM_URL;
import static org.craftercms.core.controller.rest.ContentStoreRestController.URL_CHILDREN;
import static org.craftercms.core.controller.rest.ContentStoreRestController.URL_DESCRIPTOR;
import static org.craftercms.core.controller.rest.ContentStoreRestController.URL_ITEM;
import static org.craftercms.core.controller.rest.ContentStoreRestController.URL_TREE;

/**
 * REST controller to retrieve content from the site (items and trees). It's basically a wrapper for
 * {@link ContentStoreRestController} that has already resolved the context automatically.
 *
 * @author avasquez
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteContentStoreRestController.URL_ROOT)
public class SiteContentStoreRestController extends RestControllerBase {

    public static final String URL_ROOT = "/site/content_store";

    private ContentStoreRestController wrappedController;

    @Required
    public void setWrappedController(ContentStoreRestController wrappedController) {
        this.wrappedController = wrappedController;
    }

    @RequestMapping(value = URL_DESCRIPTOR, method = RequestMethod.GET)
    public Map<String, Object> getDescriptor(WebRequest request, HttpServletResponse response,
                                             @RequestParam(REQUEST_PARAM_URL) String url) {
        return wrappedController.getDescriptor(request, response, getCurrentContextId(), url);
    }

    @RequestMapping(value = URL_ITEM, method = RequestMethod.GET)
    public Map<String, Object> getItem(WebRequest request, HttpServletResponse response,
                                       @RequestParam(REQUEST_PARAM_URL) String url) {
        return wrappedController.getItem(request, response, getCurrentContextId(), url);
    }

    @RequestMapping(value = URL_CHILDREN, method = RequestMethod.GET)
    public Map<String, Object> getChildren(WebRequest request, HttpServletResponse response,
                                           @RequestParam(REQUEST_PARAM_URL) String url) {
        return wrappedController.getChildren(request, response, getCurrentContextId(), url);
    }

    @RequestMapping(value = URL_TREE, method = RequestMethod.GET)
    public Map<String, Object> getTree(WebRequest request, HttpServletResponse response,
                                       @RequestParam(REQUEST_PARAM_URL) String url,
                                       @RequestParam(value = REQUEST_PARAM_TREE_DEPTH, required = false)
                                           Integer depth) {
        return wrappedController.getTree(request, response, getCurrentContextId(), url, depth);
    }

    protected String getCurrentContextId() {
        return SiteContext.getCurrent().getContext().getId();
    }

}
