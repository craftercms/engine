/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.commons.lang.Callback;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.util.url.ContentBundleUrlParser;
import org.craftercms.engine.mobile.UserAgentTemplateDetector;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.ScriptResolver;
import org.craftercms.engine.security.CrafterPageAccessManager;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.UrlTransformationService;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.LocaleUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

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
    protected String urlTransformerName;
    protected String fullHttpsUrlTransformerName;
    protected UrlTransformationService urlTransformationService;
    protected String localizedUrlDelimiter;
    protected ContentBundleUrlParser urlParser;
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
    protected ScriptResolver scriptResolver;
    protected ViewResolver delegatedViewResolver;
    protected boolean localizeViews;
    protected UserAgentTemplateDetector userAgentTemplateDetector;
    protected boolean modePreview;
    protected CrafterPageAccessManager accessManager;
    protected ScriptFactory scriptFactory;

    public CrafterPageViewResolver() {
        order = 10;
        cacheUrlTransformations = true;
        localizedUrlDelimiter = "_";
        cachingOptions = CachingOptions.DEFAULT_CACHING_OPTIONS;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Required
    public void setModePreview(boolean modePreview) {
        this.modePreview = modePreview;
    }

    public void setCacheUrlTransformations(boolean cacheUrlTransformations) {
        this.cacheUrlTransformations = cacheUrlTransformations;
    }

    @Required
    public void setUrlTransformerName(String urlTransformerName) {
        this.urlTransformerName = urlTransformerName;
    }

    @Required
    public void setFullHttpsUrlTransformerName(String fullHttpsUrlTransformerName) {
        this.fullHttpsUrlTransformerName = fullHttpsUrlTransformerName;
    }

    @Required
    public void setUrlTransformationService(UrlTransformationService urlTransformationService) {
        this.urlTransformationService = urlTransformationService;
    }

    public void setLocalizedUrlDelimiter(String localizedUrlDelimiter) {
        this.localizedUrlDelimiter = localizedUrlDelimiter;
    }

    @Required
    public void setUrlParser(ContentBundleUrlParser urlParser) {
        this.urlParser = urlParser;
    }

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public void setCachingOptions(CachingOptions cachingOptions) {
        this.cachingOptions = cachingOptions;
    }

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Required
    public void setPageViewNameXPathQuery(String pageViewNameXPathQuery) {
        this.pageViewNameXPathQuery = pageViewNameXPathQuery;
    }

    @Required
    public void setRedirectUrlXPathQuery(String redirectUrlXPathQuery) {
        this.redirectUrlXPathQuery = redirectUrlXPathQuery;
    }

    @Required
    public void setContentTypeXPathQuery(String contentTypeXPathQuery) {
        this.contentTypeXPathQuery = contentTypeXPathQuery;
    }

    @Required
    public void setRedirectContentType(String redirectContentType) {
        this.redirectContentType = redirectContentType;
    }

    @Required
    public void setDisabledXPathQuery(String disabledXPathQuery) {
        this.disabledXPathQuery = disabledXPathQuery;
    }

    @Required
    public void setMimeTypeXPathQuery(String mimeTypeXPathQuery) {
        this.mimeTypeXPathQuery = mimeTypeXPathQuery;
    }

    @Required
    public void setScriptResolver(ScriptResolver scriptResolver) {
        this.scriptResolver = scriptResolver;
    }

    @Required
    public void setForceHttpsXPathQuery(String forceHttpsXPathQuery) {
        this.forceHttpsXPathQuery = forceHttpsXPathQuery;
    }

    @Required
    public void setDelegatedViewResolver(ViewResolver delegatedViewResolver) {
        this.delegatedViewResolver = delegatedViewResolver;
    }

    public void setLocalizeViews(boolean localizeViews) {
        this.localizeViews = localizeViews;
    }

    @Required
    public void setUserAgentTemplateDetector(UserAgentTemplateDetector userAgentTemplateDetector) {
        this.userAgentTemplateDetector = userAgentTemplateDetector;
    }

    @Required
    public void setAccessManager(CrafterPageAccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Required
    public void setScriptFactory(ScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        String url = urlTransformationService.transform(urlTransformerName, viewName, cacheUrlTransformations);
        View view = getCachedLocalizedView(url, locale);

        if (view instanceof CrafterPageView) {
            accessManager.checkAccess(((CrafterPageView) view).getPage());
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

    protected SiteItem getLocalizedPage(String baseUrl, Locale locale) {
        List<Locale> candidateLocales = LocaleUtils.getLocaleLookupList(locale);
        for (Locale candidateLocale : candidateLocales) {
            String localizedUrl = LocaleUtils.getLocalizedUrl(urlParser, baseUrl, candidateLocale,
                    localizedUrlDelimiter);

            if (logger.isDebugEnabled()) {
                logger.debug("Retrieving localized Crafter page descriptor at " + localizedUrl + "...");
            }

            SiteItem page = siteItemService.getSiteItem(localizedUrl);
            if (page != null) {
                return page;
            } else if (logger.isDebugEnabled()) {
                logger.debug("Localized Crafter page descriptor not found at " + localizedUrl);
            }
        }

        return null;
    }

    protected View getRedirectView(String redirectUrl, boolean relative) {
        View view = new RedirectView(redirectUrl, relative, true);

        return applyLifecycleMethods(UrlBasedViewResolver.REDIRECT_URL_PREFIX + redirectUrl, view);
    }

    protected View getCurrentPageHttpsRedirectView() {
        String currentUrl = RequestContext.getCurrent().getRequest().getRequestURI();
        String fullHttpsUrl = urlTransformationService.transform(fullHttpsUrlTransformerName, currentUrl);

        return getRedirectView(fullHttpsUrl, false);
    }

    protected View getCachedLocalizedView(final String baseUrl, final Locale locale) {
        SiteContext context = SiteContext.getCurrent();

        return cacheTemplate.getObject(context.getContext(), cachingOptions, new Callback<View>() {

            @Override
            public View execute() {
                SiteItem page;
                if (localizeViews) {
                    page = getLocalizedPage(baseUrl, locale);
                } else {
                    page = getPage(baseUrl);
                }

                if (page != null) {
                    String disabled = page.getItem().queryDescriptorValue(disabledXPathQuery);
                    if (!modePreview && StringUtils.isNotEmpty(disabled) && Boolean.parseBoolean(disabled)) {
                        // when a page is disabled it acts as if it does not exist this rule does not apply in preview
                        // because we want authors to see the page
                        return null;
                    }

                    String redirectUrl = page.getItem().queryDescriptorValue(redirectUrlXPathQuery);
                    String contentType = page.getItem().queryDescriptorValue(contentTypeXPathQuery);
                    String forceHttps = page.getItem().queryDescriptorValue(forceHttpsXPathQuery);

                    if (StringUtils.isNotEmpty(contentType) &&
                        StringUtils.equalsIgnoreCase(redirectContentType, contentType) &&
                        StringUtils.isNotEmpty(redirectUrl)) {
                        return getRedirectView(redirectUrl, true);
                    } else if (StringUtils.isNotEmpty(forceHttps) && Boolean.parseBoolean(forceHttps)) {
                        return getCurrentPageHttpsRedirectView();
                    } else {
                        UserAgentAwareCrafterPageView view = new UserAgentAwareCrafterPageView();
                        view.setServletContext(getServletContext());
                        view.setPage(page);
                        view.setModePreview(modePreview);
                        view.setLocale(locale);
                        view.setSiteItemService(siteItemService);
                        view.setPageViewNameXPathQuery(pageViewNameXPathQuery);
                        view.setMimeTypeXPathQuery(mimeTypeXPathQuery);
                        view.setDelegatedViewResolver(delegatedViewResolver);
                        view.setUserAgentTemplateDetector(userAgentTemplateDetector);

                        loadScripts(page, view);

                        view.addDependencyKey(page.getItem().getKey());

                        return applyLifecycleMethods(page.getStoreUrl(), view);
                    }
                } else {
                    // Return null to continue with the ViewResolverChain
                    return null;
                }
            }

        }, baseUrl, locale, PAGE_CONST_KEY_ELEM);
    }

    protected void loadScripts(SiteItem page, CrafterPageView view) {
        List<String> scriptUrls = scriptResolver.getScriptUrls(page);
        if (CollectionUtils.isNotEmpty(scriptUrls)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Scripts associated to page " + page.getStoreUrl() + ": " + scriptUrls);
            }

            List<Script> scripts = new ArrayList<Script>(scriptUrls.size());

            for (String scriptUrl : scriptUrls) {
                Script script = scriptFactory.getScript(scriptUrl);
                scripts.add(script);
                view.addDependencyKey(script.getKey());
            }

            view.setScripts(scripts);
        }
    }

    protected View applyLifecycleMethods(String viewName, View view) {
        return (View) getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
    }

}
