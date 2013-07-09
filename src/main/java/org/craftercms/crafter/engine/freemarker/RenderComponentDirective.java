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
package org.craftercms.crafter.engine.freemarker;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.utility.DeepUnwrap;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.crafter.core.util.UrlUtils;
import org.craftercms.crafter.engine.model.SiteItem;
import org.craftercms.crafter.engine.service.SiteItemService;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Custom directive to render a component by processing the template defined in the component element's template name
 * sub-element.
 *
 * @author Alfonso Vásquez
 */
public class RenderComponentDirective implements TemplateDirectiveModel {

    public static final String COMPONENT_PARAM_NAME = "component";
    public static final String COMPONENT_PATH_PARAM_NAME = "componentPath";

    protected SiteItemService siteItemService;
    protected ObjectFactory<SimpleHash> modelFactory;
    protected String templateXPathQuery;
    protected String templateNamePrefix;
    protected String templateNameSuffix;
    protected String componentIncludeElementName;
    protected String pageModelAttributeName;
    protected String componentModelAttributeName;

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

    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException {
        TemplateModel componentParam = (TemplateModel) params.get(COMPONENT_PARAM_NAME);
        TemplateModel componentPathParam = (TemplateModel) params.get(COMPONENT_PATH_PARAM_NAME);
        SiteItem siteItem;

        if (componentParam == null && componentPathParam == null) {
            throw new TemplateException("No '" + COMPONENT_PARAM_NAME + "' or '" + COMPONENT_PATH_PARAM_NAME + "' param specified", env);
        } else if (componentParam != null) {
            siteItem = getComponentItemFromNode(componentParam, env);
        } else {
            siteItem = getComponentItemFromPath(componentPathParam, env);
        }

        Template template = getComponentTemplate(siteItem, env);
        SimpleHash model = getComponentModel(siteItem);
        Writer output = env.getOut();

        processComponentTemplate(template, model, output, env);
    }

    protected SiteItem getComponentItemFromNode(TemplateModel componentParam, Environment env) throws TemplateException {
        Object unwrappedComponentParam = DeepUnwrap.unwrap(componentParam);
        if (!(unwrappedComponentParam instanceof Node)) {
            throw new TemplateException("Param '" + COMPONENT_PARAM_NAME + " of unexpected type: expected: " + Node.class.getName() +
                    ", actual: " + unwrappedComponentParam.getClass().getName(), env);
        }

        Element includeElement = ((Element) unwrappedComponentParam).element(componentIncludeElementName);
        String componentUrl = includeElement.getTextTrim();

        return getComponentItem(componentUrl, env);
    }

    protected SiteItem getComponentItemFromPath(TemplateModel componentPathParam, Environment env) throws TemplateException {
        Object unwrappedComponentPathParam = DeepUnwrap.unwrap(componentPathParam);
        if (!(unwrappedComponentPathParam instanceof String)) {
            throw new TemplateException("Param '" + COMPONENT_PATH_PARAM_NAME + " of unexpected type: expected: " +
                    String.class.getName() + ", actual: " + unwrappedComponentPathParam.getClass().getName(), env);
        }

        return getComponentItem(((String) unwrappedComponentPathParam), env);
    }

    protected SiteItem getComponentItem(String componentPath, Environment env) throws TemplateException {
        SiteItem currentPage = (SiteItem) DeepUnwrap.unwrap(env.getVariable(pageModelAttributeName));
        if (currentPage != null) {
            try {
                componentPath = UrlUtils.resolveRelative(currentPage.getStoreUrl(), componentPath);
            } catch (URISyntaxException e) {
                throw new TemplateException("Invalid relative component URL " + componentPath, e, env);
            }
        }

        SiteItem siteItem;
        try {
           siteItem = siteItemService.getSiteItem(componentPath);
        } catch (Exception e) {
            throw new TemplateException("Unable to load component " + componentPath + " from the underlying repository", e, env);
        }

        if (siteItem == null) {
            throw new TemplateException("No component found at path '" + componentPath + "'", env);
        }

        return siteItem;
    }

    protected Template getComponentTemplate(SiteItem componentItem, Environment env) throws TemplateException {
        try {
            return env.getTemplateForInclusion(getComponentTemplateName(componentItem, env), null, true);
        } catch (IOException e) {
            throw new TemplateException("Unable to retrieve component template", e, env);
        }
    }

    protected String getComponentTemplateName(SiteItem componentItem, Environment env) throws TemplateException {
        String componentTemplateName = componentItem.getItem().queryDescriptorValue(templateXPathQuery);
        if (StringUtils.isNotEmpty(componentTemplateName)) {
            return templateNamePrefix + componentTemplateName + templateNameSuffix;
        } else {
            throw new TemplateException("No component template defined in " + componentItem, env);
        }
    }

    protected SimpleHash getComponentModel(SiteItem siteItem) throws TemplateException {
        SimpleHash componentModel = modelFactory.getObject();
        componentModel.put(componentModelAttributeName, siteItem);

        return componentModel;
    }

    protected void processComponentTemplate(Template template, SimpleHash model, Writer output, Environment env) throws TemplateException {
        try {
            template.process(model, output);
        } catch (IOException e) {
            throw new TemplateException("I/O exception while processing the component template", e, env);
        }
    }

}
