package org.craftercms.engine.controller.rest;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.controller.rest.ContentStoreRestController;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import static org.craftercms.core.controller.rest.ContentStoreRestController.* ;

/**
 * REST controller to retrieve content from the site (items and trees). It's basically a wrapper for
 * {@link ContentStoreRestController} that has already resolved the context automatically.
 *
 * @author avasquez
 */
@Controller
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteContentStoreRestController.URL_ROOT)
public class SiteContentStoreRestController {

    private static final Log logger = LogFactory.getLog(RestControllerBase.class);

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

    @ExceptionHandler(PathNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, Object>  handlePathNotFoundException(HttpServletRequest request, PathNotFoundException e) {
        return handleException(request, e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> handleException(HttpServletRequest request, Exception e) {
        logger.error("Request for " + request.getRequestURI() + " failed", e);

        return Collections.<String, Object>singletonMap(MESSAGE_MODEL_ATTRIBUTE_NAME, e.getMessage());
    }

    protected String getCurrentContextId() {
        return SiteContext.getCurrent().getContext().getId();
    }

}
