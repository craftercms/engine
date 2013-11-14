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

import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.SiteItemService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

/**
 * View resolver tha resolves to {@link CrafterFreeMarkerView}s.
 *
 * @author Alfonso Vásquez
 */
public class CrafterFreeMarkerViewResolver extends FreeMarkerViewResolver {

    protected SiteItemService siteItemService;
    protected ScriptFactory scriptFactory;
    protected String componentTemplateXPathQuery;
    protected String componentIncludeElementName;
    protected String componentScriptsXPathQuery;

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Required
    public void setScriptFactory(ScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
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
    public void setComponentScriptsXPathQuery(String componentScriptsXPathQuery) {
        this.componentScriptsXPathQuery = componentScriptsXPathQuery;
    }

    @Override
    protected Class requiredViewClass() {
        return CrafterFreeMarkerView.class;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        CrafterFreeMarkerView view = (CrafterFreeMarkerView) super.buildView(viewName);
        view.setSiteItemService(siteItemService);
        view.setScriptFactory(scriptFactory);
        view.setComponentTemplateXPathQuery(componentTemplateXPathQuery);
        view.setComponentTemplateNamePrefix(getPrefix());
        view.setComponentTemplateNameSuffix(getSuffix());
        view.setComponentIncludeElementName(componentIncludeElementName);
        view.setComponentScriptsXPathQuery(componentScriptsXPathQuery);

        return view;
    }

}
