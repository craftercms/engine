package org.craftercms.engine.controller.rest;

import java.util.List;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.navigation.NavBreadcrumbBuilder;
import org.craftercms.engine.navigation.NavItem;
import org.craftercms.engine.navigation.NavTreeBuilder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteNavigationController.URL_ROOT)
public class SiteNavigationController extends RestControllerBase {

    public static final String URL_ROOT = "/site/navigation";
    public static final String URL_TREE = "/tree";
    public static final String URL_BREADCRUMB = "/breadcrumb";

    protected NavTreeBuilder navTreeBuilder;
    protected NavBreadcrumbBuilder navBreadcrumbBuilder;

    @Required
    public void setNavTreeBuilder(final NavTreeBuilder navTreeBuilder) {
        this.navTreeBuilder = navTreeBuilder;
    }

    @Required
    public void setNavBreadcrumbBuilder(final NavBreadcrumbBuilder navBreadcrumbBuilder) {
        this.navBreadcrumbBuilder = navBreadcrumbBuilder;
    }

    @GetMapping(URL_TREE)
    public NavItem getNavTree(@RequestParam String url,
                              @RequestParam(required = false, defaultValue = "1") int depth,
                              @RequestParam(required = false, defaultValue = "") String currentPageUrl) {
        return navTreeBuilder.getNavTree(url, depth, currentPageUrl);
    }

    @GetMapping(URL_BREADCRUMB)
    public List<NavItem> getNavBreadcrumb(@RequestParam String url,
                                          @RequestParam(required = false, defaultValue = "") String root) {
        return navBreadcrumbBuilder.getBreadcrumb(url, root);
    }

}
