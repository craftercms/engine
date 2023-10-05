/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.navigation.NavBreadcrumbBuilder;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.navigation.NavTreeBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.beans.ConstructorProperties;
import java.util.List;

/**
 * REST controller to access site navigation services.
 *
 * @author joseross
 */
@Validated
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteNavigationController.URL_ROOT)
public class SiteNavigationController extends RestControllerBase {

    public static final String URL_ROOT = "/site/navigation";
    public static final String URL_TREE = "/tree";
    public static final String URL_BREADCRUMB = "/breadcrumb";

    protected final NavTreeBuilder navTreeBuilder;
    protected final NavBreadcrumbBuilder navBreadcrumbBuilder;

    @ConstructorProperties({"navTreeBuilder", "navBreadcrumbBuilder"})
    public SiteNavigationController(final NavTreeBuilder navTreeBuilder, final NavBreadcrumbBuilder navBreadcrumbBuilder) {
        this.navTreeBuilder = navTreeBuilder;
        this.navBreadcrumbBuilder = navBreadcrumbBuilder;
    }

    @GetMapping(URL_TREE)
    public NavItem getNavTree(@ValidExistingContentPath @RequestParam String url,
                              @RequestParam(required = false, defaultValue = "1") int depth,
                              @ValidExistingContentPath @RequestParam(required = false, defaultValue = "") String currentPageUrl) {
        return navTreeBuilder.getNavTree(url, depth, currentPageUrl);
    }

    @GetMapping(URL_BREADCRUMB)
    public List<NavItem> getNavBreadcrumb(@ValidExistingContentPath
                                          @RequestParam String url,
                                          @ValidExistingContentPath
                                          @RequestParam(required = false, defaultValue = "") String root) {
        return navBreadcrumbBuilder.getBreadcrumb(url, root);
    }

}
