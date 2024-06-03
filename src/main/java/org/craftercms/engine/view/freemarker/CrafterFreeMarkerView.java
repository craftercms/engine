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
package org.craftercms.engine.view.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.jakarta.servlet.HttpRequestParametersHashModel;
import freemarker.ext.jakarta.servlet.HttpSessionHashModel;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateHashModel;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.engine.freemarker.ExecuteControllerDirective;
import org.craftercms.engine.freemarker.RenderComponentDirective;
import org.craftercms.engine.freemarker.ServletContextHashModel;
import org.craftercms.engine.plugin.PluginService;
import org.craftercms.engine.scripting.SiteItemScriptResolver;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.freemarker.HttpRequestHashModel;
import org.craftercms.engine.util.freemarker.SiteContextHashModel;
import org.craftercms.engine.util.spring.ApplicationContextAccessor;
import org.craftercms.engine.util.spring.security.profile.ProfileUser;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    public static final String KEY_AUTH_TOKEN = "authToken";
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
    protected String componentEmbeddedElementName;
    protected SiteItemScriptResolver componentScriptResolver;
    protected PluginService pluginService;

    // Needed because the field in the superclass is private
    protected boolean disableVariableRestrictions;

    /**
     * Indicates if access for static methods should be allowed in Freemarker templates
     */
    protected boolean enableStatics;

    protected ServletContextHashModel servletContextHashModel;
    protected ApplicationContextAccessor applicationContextAccessor;

    @Override
    protected void initServletContext(ServletContext servletContext) throws BeansException {
        super.initServletContext(servletContext);

        servletContextHashModel = new ServletContextHashModel(servletContext, getObjectWrapper());
        applicationContextAccessor = new ApplicationContextAccessor(getApplicationContext());
    }

    @Override
    public void setExposeSpringMacroHelpers(boolean exposeSpringMacroHelpers) {
        super.setExposeSpringMacroHelpers(exposeSpringMacroHelpers);
        disableVariableRestrictions = exposeSpringMacroHelpers;
    }

    public void setEnableStatics(boolean enableStatics) {
        this.enableStatics = enableStatics;
    }

    @Autowired
    public void setSiteItemService(@Lazy SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Autowired
    public void setComponentTemplateXPathQuery(@Lazy String componentTemplateXPathQuery) {
        this.componentTemplateXPathQuery = componentTemplateXPathQuery;
    }

    @Autowired
    public void setComponentTemplateNamePrefix(@Lazy String componentTemplateNamePrefix) {
        this.componentTemplateNamePrefix = componentTemplateNamePrefix;
    }

    @Autowired
    public void setComponentTemplateNameSuffix(@Lazy String componentTemplateNameSuffix) {
        this.componentTemplateNameSuffix = componentTemplateNameSuffix;
    }

    @Autowired
    public void setComponentIncludeElementName(@Lazy String componentIncludeElementName) {
        this.componentIncludeElementName = componentIncludeElementName;
    }

    @Autowired
    public void setComponentEmbeddedElementName(@Lazy final String componentEmbeddedElementName) {
        this.componentEmbeddedElementName = componentEmbeddedElementName;
    }

    @Autowired
    public void setComponentScriptResolver(@Lazy SiteItemScriptResolver componentScriptResolver) {
        this.componentScriptResolver = componentScriptResolver;
    }

    public void setPluginService(PluginService pluginService) {
        this.pluginService = pluginService;
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
    protected SimpleHash buildTemplateModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) {
        AllHttpScopesAndAppContextHashModel templateModel = new AllHttpScopesAndAppContextHashModel(
            getObjectWrapper(), applicationContextAccessor, getServletContext(), request, disableVariableRestrictions);
        HttpSessionHashModel sessionModel = createSessionModel(request, response);
        HttpRequestHashModel requestModel =
                new HttpRequestHashModel(request, response, getObjectWrapper(), disableVariableRestrictions);
        HttpRequestParametersHashModel requestParamsModel = new HttpRequestParametersHashModel(request);
        Map<String, String> cookies = createCookieMap(request);

        if (disableVariableRestrictions) {
            templateModel.put(KEY_APPLICATION_CAP, servletContextHashModel);
            templateModel.put(KEY_APPLICATION, servletContextHashModel);
            templateModel.put(KEY_APP_CONTEXT_CAP, applicationContextAccessor);
            templateModel.put(KEY_APP_CONTEXT, applicationContextAccessor);
        }

        templateModel.put(KEY_SESSION_CAP, sessionModel);
        templateModel.put(KEY_SESSION, sessionModel);
        templateModel.put(KEY_REQUEST_CAP, requestModel);
        templateModel.put(KEY_REQUEST, requestModel);
        templateModel.put(KEY_REQUEST_PARAMS_CAP, requestParamsModel);
        templateModel.put(KEY_REQUEST_PARAMS, requestParamsModel);

        templateModel.put(KEY_COOKIES_CAP, cookies);
        templateModel.put(KEY_COOKIES, cookies);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            templateModel.put(KEY_AUTH_TOKEN, auth);

            // for backwards compatibility with Profile ...

            if (auth.getPrincipal() instanceof ProfileUser) {
                ProfileUser details = (ProfileUser) auth.getPrincipal();
                templateModel.put(KEY_AUTH_CAP, details.getAuthentication());
                templateModel.put(KEY_AUTH, details.getAuthentication());
                templateModel.put(KEY_PROFILE_CAP, details.getProfile());
                templateModel.put(KEY_PROFILE, details.getProfile());
            }
        }

        SiteContext siteContext = SiteContext.getCurrent();
        Configuration siteConfig = siteContext.getConfig();
        Locale locale = LocaleContextHolder.getLocale();
        Object siteContextObject = disableVariableRestrictions?
                siteContext : new SiteContextHashModel(getObjectWrapper());

        if (enableStatics) {
            TemplateHashModel staticModels = ((BeansWrapper) getObjectWrapper()).getStaticModels();
            templateModel.put(KEY_STATICS_CAP, staticModels);
            templateModel.put(KEY_STATICS, staticModels);
        }

        TemplateHashModel enumModels = ((BeansWrapper) getObjectWrapper()).getEnumModels();
        templateModel.put(KEY_ENUMS_CAP, enumModels);
        templateModel.put(KEY_ENUMS, enumModels);

        templateModel.put(KEY_SITE_CONTEXT_CAP, siteContextObject);
        templateModel.put(KEY_SITE_CONTEXT, siteContextObject);
        templateModel.put(KEY_LOCALE_CAP, locale);
        templateModel.put(KEY_LOCALE, locale);

        if (siteConfig != null) {
            templateModel.put(KEY_SITE_CONFIG, siteConfig);
            templateModel.put(KEY_SITE_CONFIG_CAP, siteConfig);
        }

        templateModel.putAll(model);

        pluginService.addPluginVariables(getUrl(), templateModel::put);

        ObjectFactory<SimpleHash> componentModelFactory = () -> buildTemplateModel(model, request, response);

        RenderComponentDirective renderComponentDirective = new RenderComponentDirective(getServletContext(),
                siteItemService, componentModelFactory, componentTemplateXPathQuery, componentTemplateNamePrefix,
                componentTemplateNameSuffix, componentIncludeElementName, componentEmbeddedElementName, componentScriptResolver);

        ExecuteControllerDirective executeControllerDirective = new ExecuteControllerDirective(getServletContext());

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
    	 Map<String, String> cookieMap = new HashMap<>();
    	 Cookie[] cookies = request.getCookies();

        if (ArrayUtils.isNotEmpty(cookies)) {
            for(Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
    	    }
        }
    	 
    	 return cookieMap;
    }

}
