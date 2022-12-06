/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import java.beans.ConstructorProperties;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.core.controller.rest.ContentStoreRestController;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.Tree;
import org.craftercms.engine.service.context.SiteContext;
import org.dom4j.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.HTTPURI;
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
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteContentStoreRestController.URL_ROOT)
public class SiteContentStoreRestController extends RestControllerBase {

    public static final String URL_ROOT = "/site/content_store";

    private ContentStoreRestController wrappedController;

    @ConstructorProperties({"wrappedController"})
    public SiteContentStoreRestController(ContentStoreRestController wrappedController) {
        this.wrappedController = wrappedController;
    }

    /**
     * @deprecated Will be removed in 4.1, use {@code getItem} instead
     */
    @RequestMapping(value = URL_DESCRIPTOR, method = RequestMethod.GET)
    @ValidateParams
    public Document getDescriptor(WebRequest request, HttpServletResponse response,
                                  @ValidateSecurePathParam
                                  @EsapiValidatedParam(maxLength = 4000, type = HTTPURI)
                                  @RequestParam(REQUEST_PARAM_URL) String url,
                                  @RequestParam(required = false, defaultValue = "false") boolean flatten) {
        return wrappedController.getDescriptor(request, response, getCurrentContextId(), url, flatten);
    }

    @RequestMapping(value = URL_ITEM, method = RequestMethod.GET)
    @ValidateParams
    public Item getItem(WebRequest request, HttpServletResponse response,
                        @ValidateSecurePathParam
                        @EsapiValidatedParam(maxLength = 4000, type = HTTPURI)
                        @RequestParam(REQUEST_PARAM_URL) String url,
                        @RequestParam(required = false, defaultValue = "false") boolean flatten) {
        return wrappedController.getItem(request, response, getCurrentContextId(), url, flatten);
    }

    @RequestMapping(value = URL_CHILDREN, method = RequestMethod.GET)
    @ValidateParams
    public List<Item> getChildren(WebRequest request, HttpServletResponse response,
                                  @ValidateSecurePathParam
                                  @EsapiValidatedParam(maxLength = 4000, type = HTTPURI)
                                  @RequestParam(REQUEST_PARAM_URL) String url,
                                  @RequestParam(required = false, defaultValue = "false") boolean flatten) {
        return wrappedController.getChildren(request, response, getCurrentContextId(), url, flatten);
    }

    @RequestMapping(value = URL_TREE, method = RequestMethod.GET)
    @ValidateParams
    public Tree getTree(WebRequest request, HttpServletResponse response,
                        @ValidateSecurePathParam
                        @EsapiValidatedParam(maxLength = 4000, type = HTTPURI)
                        @RequestParam(REQUEST_PARAM_URL) String url,
                        @RequestParam(value = REQUEST_PARAM_TREE_DEPTH, required = false) Integer depth,
                        @RequestParam(required = false, defaultValue = "false") boolean flatten) {
        return wrappedController.getTree(request, response, getCurrentContextId(), url, depth, flatten);
    }

    protected String getCurrentContextId() {
        return SiteContext.getCurrent().getContext().getId();
    }

}
