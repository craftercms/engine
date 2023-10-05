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

import org.craftercms.engine.plugin.PluginService;
import org.craftercms.engine.scripting.SiteItemScriptResolver;
import org.craftercms.engine.service.SiteItemService;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

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

    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    public void setComponentTemplateXPathQuery(String componentTemplateXPathQuery) {
        this.componentTemplateXPathQuery = componentTemplateXPathQuery;
    }

    public void setComponentIncludeElementName(String componentIncludeElementName) {
        this.componentIncludeElementName = componentIncludeElementName;
    }

    public void setComponentEmbeddedElementName(final String componentEmbeddedElementName) {
        this.componentEmbeddedElementName = componentEmbeddedElementName;
    }

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
    @NonNull
    protected Class<?> requiredViewClass() {
        return CrafterFreeMarkerView.class;
    }

    @Override
    @NonNull
    protected AbstractUrlBasedView buildView(@NonNull String viewName) throws Exception {
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
}
