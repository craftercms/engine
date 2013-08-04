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
package org.craftercms.engine.scripting.impl;

import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import org.craftercms.core.util.HttpServletUtils;
import org.craftercms.engine.exception.ScriptRenderingException;
import org.craftercms.engine.freemarker.ServletContextHashModel;
import org.craftercms.engine.scripting.ScriptView;
import org.craftercms.engine.scripting.Status;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * {@link ScriptView} implementation that uses FreeMarker as the underlying template engine.
 *
 * @author Alfonso Vásquez
 */
public class FreeMarkerScriptView implements ScriptView {

    protected String mimeType;
    protected Template template;
    protected ObjectWrapper objectWrapper;
    protected ServletContextHashModel servletContextHashModel;

    public FreeMarkerScriptView(String mimeType, Template template, ObjectWrapper objectWrapper, ServletContext servletContext) {
        this.mimeType = mimeType;
        this.template = template;
        this.objectWrapper = objectWrapper;
        this.servletContextHashModel = new ServletContextHashModel(servletContext, objectWrapper);
    }

    @Override
    public void render(Status status, Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws ScriptRenderingException {
        response.setContentType(mimeType);

        Object templateModel = createTemplateModel(status, model, request, response);
        processTemplate(templateModel, request, response);
    }

    protected Object createTemplateModel(Status status, Map<String, Object> model, HttpServletRequest request,
                                         HttpServletResponse response) {
        SimpleHash templateModel = new SimpleHash();
        templateModel.put("requestUrl", request.getRequestURI());
        templateModel.put("application", servletContextHashModel);
        templateModel.put("request", new HttpRequestHashModel(request, response, objectWrapper));
        templateModel.put("params", new HttpRequestParametersHashModel(request));
        templateModel.put("headers", HttpServletUtils.createHeadersMap(request));
        templateModel.put("cookies", HttpServletUtils.createCookiesMap(request));
        templateModel.put("session", createSessionModel(request, response));
        templateModel.put("status", status);
        templateModel.putAll(model);

        return templateModel;
    }

    protected HttpSessionHashModel createSessionModel(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if(session != null) {
            return new HttpSessionHashModel(session, objectWrapper);
        }
        else {
            return new HttpSessionHashModel(null, request, response, objectWrapper);
        }
    }

    protected void processTemplate(Object model, HttpServletRequest request, HttpServletResponse response) throws ScriptRenderingException {
        try {
            template.process(model, response.getWriter());
        } catch (Exception e) {
            throw new ScriptRenderingException("Error while processing template at " + getTemplateLocation(request), e);
        }
    }

    protected String getTemplateLocation(HttpServletRequest request) {
        return  "[site=" + request.getAttribute(AbstractSiteContextResolvingFilter.SITE_NAME_ATTRIBUTE) +
                ", url='" + template.getName() + "]";
    }

}
