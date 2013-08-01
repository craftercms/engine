package org.craftercms.engine.controller;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.util.HttpServletUtils;
import org.craftercms.core.util.UrlUtils;
import org.craftercms.core.util.cache.CacheCallback;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.scripting.*;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Request handler for scripted RESTful services.
 *
 * @author Alfonso Vásquez
 */
public class RestServicesRequestHandler extends WebApplicationObjectSupport implements HttpRequestHandler {

    private static final Log logger = LogFactory.getLog(RestServicesRequestHandler.class);

    private static final String SCRIPT_URL_FORMAT = "%s.%s.js"; // {url}.{method}.js

    private static final String DEFAULT_FORMAT = "json";
    private static final String DEFAULT_MIME_TYPE = "application/json";

    protected CacheTemplate cacheTemplate;
    protected ScriptFactory scriptFactory;

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Required
    public void setScriptFactory(ScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SiteContext siteContext = AbstractSiteContextResolvingFilter.getCurrentContext();

        String serviceUrl = getServiceUrl(request);
        String scriptBaseUrl = getScriptBaseUrl(serviceUrl, siteContext);
        String scriptUrl = getScriptUrl(scriptBaseUrl, request.getMethod());
        String format = getFormat(serviceUrl);
        String mimeType = getMimeType(serviceUrl);
        Map<String, Object> model = new HashMap<String, Object>();
        Status status = new Status();

        executeScript(request, siteContext, scriptUrl, model, status);
        renderView(request, response, siteContext, scriptBaseUrl, format, mimeType, model, status);
    }

    protected String getServiceUrl(HttpServletRequest request) {
        String url = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (StringUtils.isEmpty(url)) {
            throw new IllegalStateException("Required request attribute '" + HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE +
                    "' is not set");
        }

        return url;
    }

    protected String getScriptBaseUrl(String serviceUrl, SiteContext siteContext) {
        return UrlUtils.appendUrl(siteContext.getScriptsPath(), FilenameUtils.removeExtension(serviceUrl));
    }

    protected String getScriptUrl(String scriptBaseUrl, String method) {
        return String.format(SCRIPT_URL_FORMAT, scriptBaseUrl, method);
    }

    protected String getFormat(String serviceUrl) {
        String format = FilenameUtils.getExtension(serviceUrl);
        if (StringUtils.isEmpty(format)) {
            format = DEFAULT_FORMAT;
        }

        return format;
    }

    protected String getMimeType(String serviceUrl) {
        String mimeType = getServletContext().getMimeType(FilenameUtils.getName(serviceUrl));
        if (StringUtils.isEmpty(mimeType)) {
            mimeType = DEFAULT_MIME_TYPE;
        }

        return mimeType;
    }

    protected Script getScript(final SiteContext siteContext, final String url) {
        return cacheTemplate.execute(siteContext.getContext(), CachingOptions.DEFAULT_CACHING_OPTIONS, new CacheCallback<Script>() {

            @Override
            public Script doCacheable() {
                return scriptFactory.getScript(url);
            }

        }, url, "crafter.script");
    }

    protected ScriptView getView(final SiteContext siteContext, final String scriptBaseUrl, final String method, final String format,
                                 final String mimeType) {
        return cacheTemplate.execute(siteContext.getContext(), CachingOptions.DEFAULT_CACHING_OPTIONS, new CacheCallback<ScriptView>() {

            @Override
            public ScriptView doCacheable() {
                Configuration configuration = siteContext.getScriptsFreeMarkerConfig().getConfiguration();
                TemplateResolver templateResolver = new TemplateResolver(configuration);
                ObjectWrapper objectWrapper = configuration.getObjectWrapper();

                if (objectWrapper == null) {
                    objectWrapper = ObjectWrapper.DEFAULT_WRAPPER;
                }

                return new ScriptView(scriptBaseUrl, method, format, mimeType, objectWrapper, templateResolver);
            }

        }, scriptBaseUrl, method, format, "crafter.script.view");
    }

    protected Map<String, Object> createScriptVariables(HttpServletRequest request, Map<String, Object> model, Status status) {
        Map<String, Object> scriptVariables = new HashMap<String, Object>();
        scriptVariables.put("requestUrl", request.getRequestURI());
        scriptVariables.put("requestParams", request.getParameterMap());
        scriptVariables.put("headers", HttpServletUtils.createHeadersMap(request));
        scriptVariables.put("cookies", HttpServletUtils.createCookiesMap(request));
        scriptVariables.put("session", HttpServletUtils.createSessionMap(request));
        scriptVariables.put("model", model);
        scriptVariables.put("status", status);
        scriptVariables.put("logger", logger);

        return scriptVariables;
    }

    protected void executeScript(HttpServletRequest request, SiteContext siteContext, String scriptUrl, Map<String, Object> model,
                                 Status status) {
        Map<String, Object> scriptVariables = createScriptVariables(request, model, status);

        try {
            getScript(siteContext, scriptUrl).executeScript(scriptVariables);
        } catch (ScriptNotFoundException e) {
            logger.error("Script " + scriptUrl + " not found", e);

            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage(e.getMessage());
            status.setException(e);
            status.setRedirect(true);
        } catch (Exception e) {
            logger.error("Execution failed for script " + scriptUrl, e);

            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage(e.getMessage());
            status.setException(e);
            status.setRedirect(true);
        }
    }

    protected void renderView(HttpServletRequest request, HttpServletResponse response, SiteContext siteContext, String scriptBaseUrl,
                              String format, String mimeType, Map<String, Object> model, Status status) {
        Locale locale = RequestContextUtils.getLocale(request);
        ScriptView view = getView(siteContext, scriptBaseUrl, request.getMethod(), format, mimeType);

        view.render(model, status, locale, request, response);
    }

}
