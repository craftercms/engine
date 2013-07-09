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
package org.craftercms.engine.view.freemarker;

import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang.ArrayUtils;
import org.craftercms.engine.freemarker.RenderComponentDirective;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.craftercms.engine.util.spring.ApplicationContextAccessor;
import org.craftercms.security.api.RequestContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends {@link org.springframework.web.servlet.view.freemarker.FreeMarkerView} to add {@link org.craftercms.engine.freemarker.RenderComponentDirective}s to support page component rendering in Freemarker templates
 * and provide the Spring application context as part of the Freemarker model.
 *
 * @author Alfonso Vásquez
 */
public class CrafterFreeMarkerView extends FreeMarkerView {

    public static final String RENDER_COMPONENT_DIRECTIVE_NAME = "renderComponent";

    public static final String KEY_APP_CONTEXT_CAP = "ApplicationContext";
    public static final String KEY_APP_CONTEXT = "applicationContext";
    public static final String KEY_COOKIES_CAP = "Cookies";
    public static final String KEY_COOKIES = "cookies";
    public static final String KEY_PROFILE_CAP = "Profile";
    public static final String KEY_PROFILE = "profile";
    public static final String KEY_CE_CONTEXT_CAP = "CrafterEngineRequestContext";
    public static final String KEY_CE_CONTEXT = "crafterEngineRequestContext";
    
    protected SiteItemService siteItemService;
    protected String componentTemplateXPathQuery;
    protected String componentTemplateNamePrefix;
    protected String componentTemplateNameSuffix;
    protected String componentIncludeElementName;
    protected String pageModelAttributeName;
    protected String componentModelAttributeName;

    protected TemplateHashModel servletContextHashModel;
    protected ApplicationContextAccessor applicationContextAccessor;

    @Override
    protected void initServletContext(ServletContext servletContext) throws BeansException {
        super.initServletContext(servletContext);

        servletContextHashModel = new ServletContextHashModel();
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
    public void setPageModelAttributeName(String pageModelAttributeName) {
        this.pageModelAttributeName = pageModelAttributeName;
    }

    @Required
    public void setComponentModelAttributeName(String componentModelAttributeName) {
        this.componentModelAttributeName = componentModelAttributeName;
    }

    /**
     * Instead of returning the same bean from the application context, a {@link org.springframework.web.servlet.view.freemarker.FreeMarkerConfig} is returned for the current
     * {@link org.craftercms.engine.service.context.SiteContext}.
     */
    @Override
    protected FreeMarkerConfig autodetectConfiguration() throws BeansException {
        SiteContext siteContext = AbstractSiteContextResolvingFilter.getCurrentContext();

        return siteContext.getFreeMarkerConfig();
    }

    @Override
    protected SimpleHash buildTemplateModel(final Map<String, Object> model, final HttpServletRequest request,
                                            final HttpServletResponse response) {
    	RequestContext context = RequestContext.getCurrent();
    	HttpSessionHashModel sessionHashModel;
        HttpSession session = request.getSession(false);

        if(session != null) {
            sessionHashModel = new HttpSessionHashModel(session, getObjectWrapper());
        }
        else {
            sessionHashModel = new HttpSessionHashModel(null, request, response, getObjectWrapper());
        }

        AllHttpScopesAndAppContextHashModel templateModel = new AllHttpScopesAndAppContextHashModel(getObjectWrapper(),
                getApplicationContext(), getServletContext(), request);

        templateModel.put(FreemarkerServlet.KEY_APPLICATION, servletContextHashModel);
        templateModel.put(FreemarkerServlet.KEY_SESSION, sessionHashModel);
        templateModel.put(FreemarkerServlet.KEY_REQUEST, new HttpRequestHashModel(request, response, getObjectWrapper()));
        templateModel.put(FreemarkerServlet.KEY_REQUEST_PARAMETERS, new HttpRequestParametersHashModel(request));
        templateModel.put(KEY_APP_CONTEXT_CAP, applicationContextAccessor);
        templateModel.put(KEY_APP_CONTEXT, applicationContextAccessor);
        templateModel.put(KEY_COOKIES_CAP, createCookieMap(request));
        templateModel.put(KEY_COOKIES, createCookieMap(request));
        if (context != null && context.getAuthenticationToken() != null && context.getAuthenticationToken().getProfile() != null) {
            templateModel.put(KEY_PROFILE_CAP, context.getAuthenticationToken().getProfile());
            templateModel.put(KEY_PROFILE, context.getAuthenticationToken().getProfile());
        }
        templateModel.put(KEY_CE_CONTEXT_CAP, context);
        templateModel.put(KEY_CE_CONTEXT, context);

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
        renderComponentDirective.setComponentIncludeElementName(componentIncludeElementName);
        renderComponentDirective.setPageModelAttributeName(pageModelAttributeName);
        renderComponentDirective.setComponentModelAttributeName(componentModelAttributeName);

        templateModel.put(RENDER_COMPONENT_DIRECTIVE_NAME, renderComponentDirective);

        return templateModel;
    }

    protected class ServletContextHashModel implements TemplateHashModel {
        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            return getObjectWrapper().wrap(getServletContext().getAttribute(key));
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return !getServletContext().getAttributeNames().hasMoreElements();
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
