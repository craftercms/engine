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
package org.craftercms.engine.view;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.mobile.UserAgentTemplateDetector;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.properties.SiteProperties;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.SiteItemScriptResolver;
import org.craftercms.engine.security.CrafterPageAccessManager;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.UrlTransformationService;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link org.springframework.web.servlet.ViewResolver} that resolves to {@link CrafterPageView}s. This resolver
 * retrieves the Crafter page from the content store and searches in the page DOM for the actual view name. The
 * returned {@link CrafterPageView} then adds the page and the site navigation to the model, obtains the actual
 * page view name from the delegated view resolver and delegates rendering to that view.
 *
 * @author Alfonso Vasquez
 */
public class CrafterPageViewResolver extends WebApplicationObjectSupport implements ViewResolver, Ordered {

    private static final String PAGE_CONST_KEY_ELEM = "page";

    private static final Log logger = LogFactory.getLog(CrafterPageViewResolver.class);

    protected int order;
    protected boolean cacheUrlTransformations;
    protected String renderUrlToStoreUrlTransformerName;
    protected String storeUrlToRenderUrlTransformerName;
    protected String toFullHttpsUrlTransformerName;
    protected UrlTransformationService urlTransformationService;
    protected CacheTemplate cacheTemplate;
    protected CachingOptions cachingOptions;
    protected SiteItemService siteItemService;
    protected String pageViewNameXPathQuery;
    protected String redirectUrlXPathQuery;
    protected String contentTypeXPathQuery;
    protected String redirectContentType;
    protected String disabledXPathQuery;
    protected String mimeTypeXPathQuery;
    protected String forceHttpsXPathQuery;
    protected ViewResolver delegatedViewResolver;
    protected SiteItemScriptResolver scriptResolver;
    protected UserAgentTemplateDetector userAgentTemplateDetector;
    protected CrafterPageAccessManager accessManager;
    protected boolean disableVariableRestrictions;

    public CrafterPageViewResolver(String renderUrlToStoreUrlTransformerName, String storeUrlToRenderUrlTransformerName,
                                   String toFullHttpsUrlTransformerName, UrlTransformationService urlTransformationService,
                                   CacheTemplate cacheTemplate, SiteItemService siteItemService, String pageViewNameXPathQuery,
                                   String redirectUrlXPathQuery, String contentTypeXPathQuery, String redirectContentType,
                                   String disabledXPathQuery, String mimeTypeXPathQuery, String forceHttpsXPathQuery,
                                   SiteItemScriptResolver scriptResolver, ViewResolver delegatedViewResolver,
                                   UserAgentTemplateDetector userAgentTemplateDetector, CrafterPageAccessManager accessManager) {
        order = 10;
        cachingOptions = CachingOptions.DEFAULT_CACHING_OPTIONS;

        this.renderUrlToStoreUrlTransformerName = renderUrlToStoreUrlTransformerName;
        this.storeUrlToRenderUrlTransformerName = storeUrlToRenderUrlTransformerName;
        this.toFullHttpsUrlTransformerName = toFullHttpsUrlTransformerName;
        this.urlTransformationService = urlTransformationService;
        this.cacheTemplate = cacheTemplate;
        this.siteItemService = siteItemService;
        this.pageViewNameXPathQuery = pageViewNameXPathQuery;
        this.redirectUrlXPathQuery = redirectUrlXPathQuery;
        this.contentTypeXPathQuery = contentTypeXPathQuery;
        this.redirectContentType = redirectContentType;
        this.disabledXPathQuery = disabledXPathQuery;
        this.mimeTypeXPathQuery = mimeTypeXPathQuery;
        this.scriptResolver = scriptResolver;
        this.forceHttpsXPathQuery = forceHttpsXPathQuery;
        this.delegatedViewResolver = delegatedViewResolver;
        this.userAgentTemplateDetector = userAgentTemplateDetector;
        this.accessManager = accessManager;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setCacheUrlTransformations(boolean cacheUrlTransformations) {
        this.cacheUrlTransformations = cacheUrlTransformations;
    }

    public void setCachingOptions(CachingOptions cachingOptions) {
        this.cachingOptions = cachingOptions;
    }

    public void setDisableVariableRestrictions(boolean disableVariableRestrictions) {
        this.disableVariableRestrictions = disableVariableRestrictions;
    }

    @Override
    public View resolveViewName(String renderUrl, Locale locale)  {
        String storeUrl = urlTransformationService.transform(renderUrlToStoreUrlTransformerName, renderUrl, cacheUrlTransformations);
        View view = getCachedLocalizedView(storeUrl, locale);

        if (view instanceof CrafterPageView) {
            CrafterPageView pageView = (CrafterPageView)view;

            if (SiteProperties.isRedirectToTargetedUrl()) {
                String finalStoreUrl = pageView.getPage().getStoreUrl();
                String finalRenderUrl = urlTransformationService.transform(storeUrlToRenderUrlTransformerName, finalStoreUrl,
                                                                           cacheUrlTransformations);

                renderUrl = FilenameUtils.normalizeNoEndSeparator(renderUrl);
                finalRenderUrl = FilenameUtils.normalizeNoEndSeparator(finalRenderUrl);

                if (!renderUrl.equals(finalRenderUrl)) {
                    return getRedirectView(finalRenderUrl, true);
                }
            }

            accessManager.checkAccess(pageView.getPage());

            pageView.setDisableVariableRestrictions(disableVariableRestrictions);
        }

        return view;
    }

    protected SiteItem getPage(String url) {
        SiteItem page = siteItemService.getSiteItem(url);
        if (page == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Crafter page descriptor not found at " + url);
            }
        }

        return page;
    }

    protected View getRedirectView(String redirectUrl, boolean relative) {
        View view = new RedirectView(redirectUrl, relative, true);

        return applyLifecycleMethods(UrlBasedViewResolver.REDIRECT_URL_PREFIX + redirectUrl, view);
    }

    protected View getCurrentPageHttpsRedirectView() {
        String currentUrl = RequestContext.getCurrent().getRequest().getRequestURI();
        String fullHttpsUrl = urlTransformationService.transform(toFullHttpsUrlTransformerName, currentUrl);

        return getRedirectView(fullHttpsUrl, false);
    }

    protected View getCachedLocalizedView(final String url, final Locale locale) {
        final SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return cacheTemplate.getObject(siteContext.getContext(), cachingOptions, () -> {
                SiteItem page = getPage(url);
                if (page != null) {
                    String redirectUrl = page.queryValue(redirectUrlXPathQuery);
                    String contentType = page.queryValue(contentTypeXPathQuery);
                    String forceHttps = page.queryValue(forceHttpsXPathQuery);

                    if (StringUtils.isNotEmpty(contentType) &&
                        contentType.matches(redirectContentType) &&
                        StringUtils.isNotEmpty(redirectUrl)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Redirecting page @ " + url + " to URL " + redirectUrl);
                        }

                        return getRedirectView(redirectUrl, true);
                    } else if (StringUtils.isNotEmpty(forceHttps) && Boolean.parseBoolean(forceHttps)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Forcing HTTPS on page @ " + url);
                        }

                        return getCurrentPageHttpsRedirectView();
                    } else {
                        UserAgentAwareCrafterPageView view = new UserAgentAwareCrafterPageView(
                                page, locale, pageViewNameXPathQuery, mimeTypeXPathQuery,
                                loadScripts(siteContext.getScriptFactory(), page),
                                delegatedViewResolver, userAgentTemplateDetector);
                        view.setServletContext(getServletContext());
                        return applyLifecycleMethods(page.getStoreUrl(), view);
                    }
                } else {
                    // Return null to continue with the ViewResolverChain
                    return null;
                }
            }, url, locale, PAGE_CONST_KEY_ELEM);
        } else {
            // Return null to continue with the ViewResolverChain
            return null;
        }
    }

    protected  List<Script> loadScripts(ScriptFactory scriptFactory, SiteItem page) {
        if (scriptFactory == null) {
            return new ArrayList<>();
        }

        List<String> scriptUrls = scriptResolver.getScriptUrls(page);
        if (CollectionUtils.isEmpty(scriptUrls)) {
            return new ArrayList<>();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Scripts associated to page " + page.getStoreUrl() + ": " + scriptUrls);
        }

        List<Script> scripts = new ArrayList<>(scriptUrls.size());

        for (String scriptUrl : scriptUrls) {
            Script script = scriptFactory.getScript(scriptUrl);
            scripts.add(script);
        }
        return scripts;
    }

    protected View applyLifecycleMethods(String viewName, View view) {
        return (View) getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
    }

}
