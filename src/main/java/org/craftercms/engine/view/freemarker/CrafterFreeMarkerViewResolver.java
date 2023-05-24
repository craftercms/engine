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

import org.apache.commons.lang.StringUtils;
import org.craftercms.engine.plugin.PluginService;
import org.craftercms.engine.scripting.SiteItemScriptResolver;
import org.craftercms.engine.service.SiteItemService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * View resolver tha resolves to {@link CrafterFreeMarkerView}s.
 *
 * @author Alfonso Vásquez
 */
public class CrafterFreeMarkerViewResolver extends FreeMarkerViewResolver {

    protected SiteItemService siteItemService;
    protected String componentTemplateXPathQuery;
    protected String componentIncludeElementName;
    protected String componentEmbeddedElementName;
    protected SiteItemScriptResolver componentScriptResolver;
    protected PluginService pluginService;

    /**
     * Indicates if access for static methods should be allowed in Freemarker templates
     */
    protected boolean enableStatics;

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Required
    public void setComponentTemplateXPathQuery(String componentTemplateXPathQuery) {
        this.componentTemplateXPathQuery = componentTemplateXPathQuery;
    }

    @Required
    public void setComponentIncludeElementName(String componentIncludeElementName) {
        this.componentIncludeElementName = componentIncludeElementName;
    }

    @Required
    public void setComponentEmbeddedElementName(final String componentEmbeddedElementName) {
        this.componentEmbeddedElementName = componentEmbeddedElementName;
    }

    @Required
    public void setComponentScriptResolver(SiteItemScriptResolver componentScriptResolver) {
        this.componentScriptResolver = componentScriptResolver;
    }

    public void setEnableStatics(boolean enableStatics) {
        this.enableStatics = enableStatics;
    }

    public void setPluginService(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Override
    protected Class requiredViewClass() {
        return CrafterFreeMarkerView.class;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        CrafterFreeMarkerView view = (CrafterFreeMarkerView) super.buildView(viewName);
        view.setSiteItemService(siteItemService);
        view.setComponentTemplateXPathQuery(componentTemplateXPathQuery);
        view.setComponentTemplateNamePrefix(getPrefix());
        view.setComponentTemplateNameSuffix(getSuffix());
        view.setComponentIncludeElementName(componentIncludeElementName);
        view.setComponentEmbeddedElementName(componentEmbeddedElementName);
        view.setComponentScriptResolver(componentScriptResolver);
        view.setEnableStatics(enableStatics);
        view.setPluginService(pluginService);

        return view;
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            String pageUrl = (String) requestAttributes
                    .getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            if (isNotEmpty(pageUrl) && StringUtils.equals(pageUrl, viewName)) {
                return null;
            }
        }
        return super.resolveViewName(viewName, locale);
    }
}
