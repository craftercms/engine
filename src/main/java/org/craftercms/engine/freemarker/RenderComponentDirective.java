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
package org.craftercms.engine.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

import freemarker.core.Environment;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.utility.DeepUnwrap;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.SiteItemScriptResolver;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.GroovyScriptUtils;
import org.craftercms.engine.view.CrafterPageView;
import org.dom4j.Element;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Custom directive to render a component by processing the template defined in the component element's template name
 * sub-element.
 *
 * @author Alfonso Vásquez
 */
public class RenderComponentDirective implements TemplateDirectiveModel {

    private static final Log logger = LogFactory.getLog(RenderComponentDirective.class);

    @Deprecated
    public static final String KEY_MODEL = "model";
    public static final String KEY_CONTENT_MODEL = "contentModel";

    public static final String COMPONENT_PARAM_NAME = "component";
    public static final String COMPONENT_PATH_PARAM_NAME = "componentPath";
    public static final String ADDITIONAL_MODEL_PARAM_NAME = "additionalModel";

    protected ServletContext servletContext;
    protected SiteItemService siteItemService;
    protected ObjectFactory<SimpleHash> modelFactory;
    protected String templateXPathQuery;
    protected String templateNamePrefix;
    protected String templateNameSuffix;
    protected String includeElementName;
    protected SiteItemScriptResolver scriptResolver;

    @Required
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Required
    public void setModelFactory(ObjectFactory<SimpleHash> modelFactory) {
        this.modelFactory = modelFactory;
    }

    @Required
    public void setTemplateXPathQuery(String templateXPathQuery) {
        this.templateXPathQuery = templateXPathQuery;
    }

    @Required
    public void setTemplateNamePrefix(String templateNamePrefix) {
        this.templateNamePrefix = templateNamePrefix;
    }

    @Required
    public void setTemplateNameSuffix(String templateNameSuffix) {
        this.templateNameSuffix = templateNameSuffix;
    }

    @Required
    public void setIncludeElementName(String includeElementName) {
        this.includeElementName = includeElementName;
    }

    @Required
    public void setScriptResolver(SiteItemScriptResolver scriptResolver) {
        this.scriptResolver = scriptResolver;
    }

    @SuppressWarnings("unchecked")
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException {
        TemplateModel componentParam = (TemplateModel) params.get(COMPONENT_PARAM_NAME);
        TemplateModel componentPathParam = (TemplateModel) params.get(COMPONENT_PATH_PARAM_NAME);
        TemplateModel additionalModelParam = (TemplateModel) params.get(ADDITIONAL_MODEL_PARAM_NAME);
        Map<String, Object> additionalModel = null;
        SiteItem component;

        if (componentParam == null && componentPathParam == null) {
            throw new TemplateException("No '" + COMPONENT_PARAM_NAME + "' or '" + COMPONENT_PATH_PARAM_NAME +
                                        "' param specified", env);
        } else if (componentParam != null) {
            component = getComponentFromNode(componentParam, env);
        } else {
            component = getComponentFromPath(componentPathParam, env);
        }

        if (additionalModelParam != null) {
            additionalModel = unwrap(ADDITIONAL_MODEL_PARAM_NAME, additionalModelParam, Map.class, env);
        }

        Map<String, Object> templateModel = executeScripts(component, additionalModel, env);
        SimpleHash model = getFullModel(component, templateModel, additionalModel);
        Template template = getTemplate(component, env);
        Writer output = env.getOut();

        processComponentTemplate(template, model, output, env);
    }

    protected SiteItem getComponentFromNode(TemplateModel componentParam, Environment env) throws TemplateException {
        Element includeElementParent = unwrap(COMPONENT_PARAM_NAME, componentParam, Element.class, env);
        Element includeElement = includeElementParent.element(includeElementName);

        if (includeElement != null) {
            String componentPath = includeElement.getTextTrim();

            return getComponent(componentPath, env);
        } else {
            throw new IllegalStateException("No '" + includeElementName + "' element found under component " +
                                            includeElementParent.getUniquePath());
        }
    }

    protected SiteItem getComponentFromPath(TemplateModel componentPathParam, Environment env) throws TemplateException {
        String componentPath = unwrap(COMPONENT_PATH_PARAM_NAME, componentPathParam, String.class, env);

        return getComponent(componentPath, env);
    }

    protected SiteItem getComponent(String componentPath, Environment env) throws TemplateException {
        SiteItem currentPage = unwrap(KEY_CONTENT_MODEL, env.getVariable(CrafterPageView.KEY_CONTENT_MODEL), SiteItem.class, env);
        if (currentPage != null) {
            String basePath = FilenameUtils.getFullPath(currentPage.getStoreUrl());
            URI baseUri = URI.create(basePath);

            try {
                componentPath = baseUri.resolve(componentPath).toString();
            } catch (IllegalArgumentException e) {
                throw new TemplateException("Invalid relative component URL " + componentPath, e, env);
            }
        }

        SiteItem component;
        try {
           component = siteItemService.getSiteItem(componentPath);
        } catch (Exception e) {
            throw new TemplateException("Unable to load component " + componentPath + " from the underlying repository", e, env);
        }

        if (component == null) {
            throw new TemplateException("No component found at path '" + componentPath + "'", env);
        }

        return component;
    }

    protected Map<String, Object> executeScripts(SiteItem component, Map<String, Object> additionalModel,
                                                 Environment env) throws TemplateException {
        List<String> scriptUrls = scriptResolver.getScriptUrls(component);
        if (CollectionUtils.isNotEmpty(scriptUrls)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Scripts associated to component " + component.getStoreUrl() + ": " + scriptUrls);
            }

            Map<String, Object> templateModel = new HashMap<>();
            Map<String, Object> scriptVariables = createScriptVariables(component, templateModel, additionalModel);
            SiteContext siteContext = SiteContext.getCurrent();

            if (siteContext != null) {
                ScriptFactory scriptFactory = siteContext.getScriptFactory();

                if (scriptFactory == null) {
                    throw new IllegalStateException("No script factory associate to current site context '" +
                                                    siteContext.getSiteName() + "'");
                }

                for (String scriptUrl : scriptUrls) {
                    Script script;
                    try {
                        script = scriptFactory.getScript(scriptUrl);
                    } catch (Exception e) {
                        throw new TemplateException("Unable to load script at '" + scriptUrl + "'", e, env);
                    }

                    executeScript(script, scriptVariables, env);
                }
            } else {
                throw new IllegalStateException("No current site context found");
            }

            return templateModel;
        } else {
            return null;
        }
    }

    protected Map<String, Object> createScriptVariables(SiteItem component, Map<String, Object> templateModel,
                                                        Map<String, Object> additionalModel) {
        Map<String, Object> variables = new HashMap<>();
        RequestContext context = RequestContext.getCurrent();

        if (context != null) {
            GroovyScriptUtils.addSiteItemScriptVariables(variables, context.getRequest(), context.getResponse(), servletContext,
                                                         component, templateModel);

            if (MapUtils.isNotEmpty(additionalModel)) {
                variables.putAll(additionalModel);
            }
        } else {
            throw new IllegalStateException("No current request context found");
        }

        return variables;
    }

    protected void executeScript(Script script, Map<String, Object> scriptVariables, Environment env)
        throws TemplateException {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing component script at " + script.getUrl());
        }

        try {
            script.execute(scriptVariables);
        } catch (Exception e) {
            throw new TemplateException("Error executing component script at " + script.getUrl(), e, env);
        }
    }

    protected Template getTemplate(SiteItem component, Environment env) throws TemplateException {
        try {
            return env.getTemplateForInclusion(getComponentTemplateName(component, env), null, true);
        } catch (IOException e) {
            throw new TemplateException("Unable to retrieve component template", e, env);
        }
    }

    protected String getComponentTemplateName(SiteItem component, Environment env) throws TemplateException {
        String componentTemplateName = component.getItem().queryDescriptorValue(templateXPathQuery);
        if (StringUtils.isNotEmpty(componentTemplateName)) {
            return templateNamePrefix + componentTemplateName + templateNameSuffix;
        } else {
            throw new TemplateException("No component template defined in " + component, env);
        }
    }

    protected SimpleHash getFullModel(SiteItem component, Map<String, Object> templateModel,
                                      Map<String, Object> additionalModel) throws TemplateException {
        SimpleHash model = modelFactory.getObject();
        model.put(KEY_MODEL, component);
        model.put(KEY_CONTENT_MODEL, component);

        if (MapUtils.isNotEmpty(templateModel)) {
            model.putAll(templateModel);
        }
        if (MapUtils.isNotEmpty(additionalModel)) {
            model.putAll(additionalModel);
        }

        return model;
    }

    protected void processComponentTemplate(Template template, SimpleHash model, Writer output, Environment env)
        throws TemplateException {
        try {
            template.process(model, output);
        } catch (IOException e) {
            throw new TemplateException("I/O exception while processing the component template", e, env);
        }
    }

    protected <T> T unwrap(String name, TemplateModel value, Class<T> expectedClass, Environment env) throws TemplateException {
        if (value != null) {
            Object unwrappedValue = DeepUnwrap.unwrap(value);
            try {
                return expectedClass.cast(unwrappedValue);
            } catch (ClassCastException e) {
                throw new TemplateException("Model value '" + name + "' of unexpected type: expected: " + expectedClass.getName() +
                                            ", actual: " + unwrappedValue.getClass().getName(), env);
            }
        } else {
            return null;
        }
    }

}
