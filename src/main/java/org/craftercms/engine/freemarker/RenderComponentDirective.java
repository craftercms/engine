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
package org.craftercms.engine.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.util.UrlUtils;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.ScriptResolver;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.GroovyScriptUtils;
import org.craftercms.engine.view.CrafterPageView;
import org.dom4j.Element;
import org.dom4j.Node;
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

    protected ServletContext servletContext;
    protected SiteItemService siteItemService;
    protected ObjectFactory<SimpleHash> modelFactory;
    protected String templateXPathQuery;
    protected String templateNamePrefix;
    protected String templateNameSuffix;
    protected String includeElementName;
    protected ScriptResolver scriptResolver;

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
    public void setScriptResolver(ScriptResolver scriptResolver) {
        this.scriptResolver = scriptResolver;
    }

    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
        throws TemplateException {
        TemplateModel componentParam = (TemplateModel) params.get(COMPONENT_PARAM_NAME);
        TemplateModel componentPathParam = (TemplateModel) params.get(COMPONENT_PATH_PARAM_NAME);
        SiteItem component;

        if (componentParam == null && componentPathParam == null) {
            throw new TemplateException("No '" + COMPONENT_PARAM_NAME + "' or '" + COMPONENT_PATH_PARAM_NAME +
                                        "' param specified", env);
        } else if (componentParam != null) {
            component = getComponentFromNode(componentParam, env);
        } else {
            component = getComponentFromPath(componentPathParam, env);
        }

        Map<String, Object> scriptsModel = executeScripts(component, env);
        SimpleHash model = getComponentModel(component, scriptsModel);
        Template template = getComponentTemplate(component, env);
        Writer output = env.getOut();

        processComponentTemplate(template, model, output, env);
    }

    protected SiteItem getComponentFromNode(TemplateModel componentParam, Environment env) throws TemplateException {
        Object unwrappedComponentParam = DeepUnwrap.unwrap(componentParam);
        if (!(unwrappedComponentParam instanceof Node)) {
            throw new TemplateException("Param '" + COMPONENT_PARAM_NAME + " of unexpected type: expected: " +
                                        Node.class.getName() + ", actual: " +
                                        unwrappedComponentParam.getClass().getName(), env);
        }

        Element includeElement = ((Element) unwrappedComponentParam).element(includeElementName);
        String componentUrl = includeElement.getTextTrim();

        return getComponent(componentUrl, env);
    }

    protected SiteItem getComponentFromPath(TemplateModel componentPathParam, Environment env)
        throws TemplateException {
        Object unwrappedComponentPathParam = DeepUnwrap.unwrap(componentPathParam);
        if (!(unwrappedComponentPathParam instanceof String)) {
            throw new TemplateException("Param '" + COMPONENT_PATH_PARAM_NAME + " of unexpected type: expected: " +
                    String.class.getName() + ", actual: " + unwrappedComponentPathParam.getClass().getName(), env);
        }

        return getComponent(((String) unwrappedComponentPathParam), env);
    }

    protected SiteItem getComponent(String componentPath, Environment env) throws TemplateException {
        Object unwrappedCurrentPage = DeepUnwrap.unwrap(env.getVariable(CrafterPageView.KEY_CONTENT_MODEL));
        if (unwrappedCurrentPage != null && unwrappedCurrentPage instanceof SiteItem) {
            SiteItem currentPage = (SiteItem) unwrappedCurrentPage;

            try {
                componentPath = UrlUtils.resolveRelative(currentPage.getStoreUrl(), componentPath);
            } catch (URISyntaxException e) {
                throw new TemplateException("Invalid relative component URL " + componentPath, e, env);
            }
        }

        SiteItem component;
        try {
           component = siteItemService.getSiteItem(componentPath);
        } catch (Exception e) {
            throw new TemplateException("Unable to load component " + componentPath +
                                        " from the underlying repository", e, env);
        }

        if (component == null) {
            throw new TemplateException("No component found at path '" + componentPath + "'", env);
        }

        return component;
    }

    protected Map<String, Object> executeScripts(SiteItem component, Environment env) throws TemplateException {
        List<String> scriptUrls = scriptResolver.getScriptUrls(component);
        if (CollectionUtils.isNotEmpty(scriptUrls)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Scripts associated to component " + component.getStoreUrl() + ": " + scriptUrls);
            }

            Map<String, Object> model = new HashMap<String, Object>();
            Map<String, Object> scriptVariables = createScriptVariables(component, model);
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

            return model;
        } else {
            return null;
        }
    }

    protected Map<String, Object> createScriptVariables(SiteItem component, Map<String, Object> model) {
        Map<String, Object> variables = new HashMap<String, Object>();
        RequestContext context = RequestContext.getCurrent();

        GroovyScriptUtils.addComponentScriptVariables(variables, context.getRequest(), context.getResponse(),
                                                      servletContext, component, model);

        return variables;
    }

    protected void executeScript(Script script, Map<String, Object> scriptVariables, Environment env)
        throws TemplateException {
        try {
            script.execute(scriptVariables);
        } catch (Exception e) {
            throw new TemplateException("Error executing component script at " + script.getUrl(), e, env);
        }
    }

    protected Template getComponentTemplate(SiteItem component, Environment env) throws TemplateException {
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

    protected SimpleHash getComponentModel(SiteItem component,
                                           Map<String, Object> scriptsModel) throws TemplateException {
        SimpleHash componentModel = modelFactory.getObject();
        componentModel.put(KEY_MODEL, component);
        componentModel.put(KEY_CONTENT_MODEL, component);

        if (MapUtils.isNotEmpty(scriptsModel)) {
            componentModel.putAll(scriptsModel);
        }

        return componentModel;
    }

    protected void processComponentTemplate(Template template, SimpleHash model, Writer output, Environment env)
        throws TemplateException {
        try {
            template.process(model, output);
        } catch (IOException e) {
            throw new TemplateException("I/O exception while processing the component template", e, env);
        }
    }

}
