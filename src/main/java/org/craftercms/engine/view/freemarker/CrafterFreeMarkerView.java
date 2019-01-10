/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.view.freemarker;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateHashModel;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.engine.freemarker.ExecuteControllerDirective;
import org.craftercms.engine.freemarker.RenderComponentDirective;
import org.craftercms.engine.freemarker.ServletContextHashModel;
import org.craftercms.engine.scripting.SiteItemScriptResolver;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.freemarker.HttpRequestHashModel;
import org.craftercms.engine.util.spring.ApplicationContextAccessor;
import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.utils.SecurityUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

/**
 * Extends {@link FreeMarkerView} to add {@link RenderComponentDirective}s to support page component rendering in
 * Freemarker templates and provide the Spring application context as part of the Freemarker model.
 *
 * @author Alfonso Vásquez
 */
public class CrafterFreeMarkerView extends FreeMarkerView {

    public static final String RENDER_COMPONENT_DIRECTIVE_NAME = "renderComponent";
    public static final String EXECUTE_CONTROLLER_DIRECTIVE_NAME = "controller";

    public static final String KEY_APPLICATION_CAP = "Application";
    public static final String KEY_APPLICATION = "application";
    public static final String KEY_SESSION_CAP = "Session";
    public static final String KEY_SESSION = "session";
    public static final String KEY_REQUEST_CAP = "Request";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_REQUEST_PARAMS_CAP = "RequestParameters";
    public static final String KEY_REQUEST_PARAMS = "requestParameters";
    public static final String KEY_APP_CONTEXT_CAP = "ApplicationContext";
    public static final String KEY_APP_CONTEXT = "applicationContext";
    public static final String KEY_COOKIES_CAP = "Cookies";
    public static final String KEY_COOKIES = "cookies";
    public static final String KEY_AUTH_CAP = "Authentication";
    public static final String KEY_AUTH = "authentication";
    public static final String KEY_PROFILE_CAP = "Profile";
    public static final String KEY_PROFILE = "profile";
    public static final String KEY_STATICS_CAP = "Statics";
    public static final String KEY_STATICS = "statics";
    public static final String KEY_ENUMS_CAP = "Enums";
    public static final String KEY_ENUMS = "enums";
    public static final String KEY_SITE_CONTEXT = "siteContext";
    public static final String KEY_SITE_CONTEXT_CAP = "SiteContext";
    public static final String KEY_SITE_CONFIG = "siteConfig";
    public static final String KEY_SITE_CONFIG_CAP = "SiteConfig";
    public static final String KEY_LOCALE = "locale";
    public static final String KEY_LOCALE_CAP = "Locale";
    
    protected SiteItemService siteItemService;
    protected String componentTemplateXPathQuery;
    protected String componentTemplateNamePrefix;
    protected String componentTemplateNameSuffix;
    protected String componentIncludeElementName;
    protected SiteItemScriptResolver componentScriptResolver;

    protected ServletContextHashModel servletContextHashModel;
    protected ApplicationContextAccessor applicationContextAccessor;

    @Override
    protected void initServletContext(ServletContext servletContext) throws BeansException {
        super.initServletContext(servletContext);

        servletContextHashModel = new ServletContextHashModel(servletContext, getObjectWrapper());
        applicationContextAccessor = new ApplicationContextAccessor(getApplicationContext());
    }

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Required
    public void setComponentTemplateXPathQuery(String componentTemplateXPathQuery) {
        this.componentTemplateXPathQuery = componentTemplateXPathQuery;
    }

    @Required
    public void setComponentTemplateNamePrefix(String componentTemplateNamePrefix) {
        this.componentTemplateNamePrefix = componentTemplateNamePrefix;
    }

    @Required
    public void setComponentTemplateNameSuffix(String componentTemplateNameSuffix) {
        this.componentTemplateNameSuffix = componentTemplateNameSuffix;
    }

    @Required
    public void setComponentIncludeElementName(String componentIncludeElementName) {
        this.componentIncludeElementName = componentIncludeElementName;
    }

    @Required
    public void setComponentScriptResolver(SiteItemScriptResolver componentScriptResolver) {
        this.componentScriptResolver = componentScriptResolver;
    }

    /**
     * Instead of returning the same bean from the application context, a {@link FreeMarkerConfig} is returned for
     * the current {@link SiteContext}.
     */
    @Override
    protected FreeMarkerConfig autodetectConfiguration() throws BeansException {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return siteContext.getFreeMarkerConfig();
        } else {
            return super.autodetectConfiguration();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected SimpleHash buildTemplateModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) {
        AllHttpScopesAndAppContextHashModel templateModel = new AllHttpScopesAndAppContextHashModel(
            getObjectWrapper(), applicationContextAccessor, getServletContext(), request);
        HttpSessionHashModel sessionModel = createSessionModel(request, response);
        HttpRequestHashModel requestModel = new HttpRequestHashModel(request, response, getObjectWrapper());
        HttpRequestParametersHashModel requestParamsModel = new HttpRequestParametersHashModel(request);
        Map<String, String> cookies = createCookieMap(request);

        templateModel.put(KEY_APPLICATION_CAP, servletContextHashModel);
        templateModel.put(KEY_APPLICATION, servletContextHashModel);
        templateModel.put(KEY_SESSION_CAP, sessionModel);
        templateModel.put(KEY_SESSION, sessionModel);
        templateModel.put(KEY_REQUEST_CAP, requestModel);
        templateModel.put(KEY_REQUEST, requestModel);
        templateModel.put(KEY_REQUEST_PARAMS_CAP, requestParamsModel);
        templateModel.put(KEY_REQUEST_PARAMS, requestParamsModel);
        templateModel.put(KEY_APP_CONTEXT_CAP, applicationContextAccessor);
        templateModel.put(KEY_APP_CONTEXT, applicationContextAccessor);
        templateModel.put(KEY_COOKIES_CAP, cookies);
        templateModel.put(KEY_COOKIES, cookies);

        Authentication auth = SecurityUtils.getAuthentication(request);
        if (auth != null) {
            templateModel.put(KEY_AUTH_CAP, auth);
            templateModel.put(KEY_AUTH, auth);
            templateModel.put(KEY_PROFILE_CAP, auth.getProfile());
            templateModel.put(KEY_PROFILE, auth.getProfile());
        }

        SiteContext siteContext = SiteContext.getCurrent();
        Configuration siteConfig = siteContext.getConfig();
        Locale locale = LocaleContextHolder.getLocale();
        TemplateHashModel staticModels = BeansWrapper.getDefaultInstance().getStaticModels();
        TemplateHashModel enumModels = BeansWrapper.getDefaultInstance().getEnumModels();

        templateModel.put(KEY_STATICS_CAP, staticModels);
        templateModel.put(KEY_STATICS, staticModels);
        templateModel.put(KEY_ENUMS_CAP, enumModels);
        templateModel.put(KEY_ENUMS, enumModels);
        templateModel.put(KEY_SITE_CONTEXT_CAP, siteContext);
        templateModel.put(KEY_SITE_CONTEXT, siteContext);
        templateModel.put(KEY_LOCALE_CAP, locale);
        templateModel.put(KEY_LOCALE, locale);

        if (siteConfig != null) {
            templateModel.put(KEY_SITE_CONFIG, siteConfig);
            templateModel.put(KEY_SITE_CONFIG_CAP, siteConfig);
        }

        templateModel.putAll(model);

        ObjectFactory<SimpleHash> componentModelFactory = new ObjectFactory<SimpleHash>() {
            public SimpleHash getObject() {
                return buildTemplateModel(model, request, response);
            }
        };

        RenderComponentDirective renderComponentDirective = new RenderComponentDirective();
        renderComponentDirective.setSiteItemService(siteItemService);
        renderComponentDirective.setModelFactory(componentModelFactory);
        renderComponentDirective.setTemplateXPathQuery(componentTemplateXPathQuery);
        renderComponentDirective.setTemplateNamePrefix(componentTemplateNamePrefix);
        renderComponentDirective.setTemplateNameSuffix(componentTemplateNameSuffix);
        renderComponentDirective.setIncludeElementName(componentIncludeElementName);
        renderComponentDirective.setScriptResolver(componentScriptResolver);
        renderComponentDirective.setServletContext(getServletContext());

        ExecuteControllerDirective executeControllerDirective = new ExecuteControllerDirective();
        executeControllerDirective.setServletContext(getServletContext());

        templateModel.put(RENDER_COMPONENT_DIRECTIVE_NAME, renderComponentDirective);
        templateModel.put(EXECUTE_CONTROLLER_DIRECTIVE_NAME, executeControllerDirective);

        return templateModel;
    }

    protected HttpSessionHashModel createSessionModel(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            return new HttpSessionHashModel(session, getObjectWrapper());
        }
        else {
            return new HttpSessionHashModel(null, request, response, getObjectWrapper());
        }
    }

    protected Map<String, String> createCookieMap(HttpServletRequest request) {
    	 Map<String, String> cookieMap = new HashMap<String, String>();
    	 Cookie[] cookies = request.getCookies();

        if (ArrayUtils.isNotEmpty(cookies)) {
            for(Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
    	    }
        }
    	 
    	 return cookieMap;
    }

}
