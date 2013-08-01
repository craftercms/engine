package org.craftercms.engine.scripting;

import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.ext.servlet.HttpSessionHashModel;
import freemarker.template.*;
import org.craftercms.core.util.HttpServletUtils;
import org.craftercms.engine.exception.ScriptRenderingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Renders the response of a script.
 *
 * @author Alfonso Vásquez
 */
public class ScriptView {

    protected String scriptBaseUrl;
    protected String method;
    protected String format;
    protected String mimeType;
    protected ObjectWrapper objectWrapper;
    protected TemplateResolver templateResolver;

    public ScriptView(String scriptBaseUrl, String method, String format, String mimeType, ObjectWrapper objectWrapper,
                      TemplateResolver templateResolver) {
        this.scriptBaseUrl = scriptBaseUrl;
        this.method = method;
        this.format = format;
        this.mimeType = mimeType;
        this.objectWrapper = objectWrapper;
        this.templateResolver = templateResolver;
    }

    public void render(Map<String, Object> model, Status status, Locale locale, HttpServletRequest request, HttpServletResponse response)
            throws ScriptRenderingException {
        response.setContentType(mimeType);

        Object templateModel = createTemplateModel(model, status, request, response);
        Template template = getTemplate(status, locale);

        processTemplate(template, templateModel, response);
    }

    protected Object createTemplateModel(Map<String, Object> model, Status status, HttpServletRequest request,
                                         HttpServletResponse response) {
        SimpleHash templateModel = new SimpleHash();
        templateModel.put("requestUrl", request.getRequestURI());
        templateModel.put("requestAttributes", new HttpRequestHashModel(request, response, objectWrapper));
        templateModel.put("requestParams", new HttpRequestParametersHashModel(request));
        templateModel.put("headers", HttpServletUtils.createHeadersMap(request));
        templateModel.put("cookies", HttpServletUtils.createCookiesMap(request));
        templateModel.put("session", createSessionModel(request, response));
        templateModel.put("status", status);
        templateModel.putAll(model);

        return templateModel;
    }

    protected HttpSessionHashModel createSessionModel(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return new HttpSessionHashModel(session, objectWrapper);
        } else {
            return new HttpSessionHashModel(null, request, response, objectWrapper);
        }
    }

    protected Template getTemplate(Status status, Locale locale) throws ScriptRenderingException {
        try {
            Template template = templateResolver.resolveTemplate(scriptBaseUrl, method, format, status, locale);
            if (template == null) {
                throw new ScriptRenderingException("No template found for " + getScriptDescription());
            }

            return template;
        } catch (IOException e) {
            throw new ScriptRenderingException("Unable to retrieve template for " + getScriptDescription());
        }
    }

    protected void processTemplate(Template template, Object model, HttpServletResponse response) throws ScriptRenderingException {
        try {
            template.process(model, response.getWriter());
        } catch (Exception e) {
            throw new ScriptRenderingException("Error while processing template for " + getScriptDescription(), e);
        }
    }

    protected String getScriptDescription() {
        return  "[scriptBaseUrl='" + scriptBaseUrl + "'" +
                ", method='" + method + "'" +
                ", format='" + format + "']";
    }

}
