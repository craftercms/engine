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

package org.craftercms.engine.service;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.macro.MacroResolver;
import org.craftercms.engine.mobile.UserAgentTemplateDetector;
import org.springframework.beans.factory.annotation.Required;

public class PreviewOverlayCallback {

    private String scriptFormat;
	private String[] previewServerJsScriptSources;
    private MacroResolver macroResolver;
    private UserAgentTemplateDetector userAgentTemplateDetector;

    @Required
    public void setScriptFormat(String scriptFormat) {
        this.scriptFormat = scriptFormat;
    }

    @Required
    public void setPreviewServerJsScriptSources(String[] previewServerJsScriptSources) {
        this.previewServerJsScriptSources = previewServerJsScriptSources;
    }

    @Required
    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    @Required
    public void setUserAgentTemplateDetector(UserAgentTemplateDetector userAgentTemplateDetector) {
        this.userAgentTemplateDetector = userAgentTemplateDetector;
    }

    public String render() {
        String queryString = RequestContext.getCurrent().getRequest().getQueryString();
		StringBuilder scriptsStr = new StringBuilder();

        // TODO: Shouldn't we also check if CStudio-Agent header is also present?
		if(StringUtils.isEmpty(queryString) ||
           !queryString.contains(userAgentTemplateDetector.getAgentQueryStringParamName())) {
			for (String scriptSrc : previewServerJsScriptSources) {
                String script = scriptFormat.replace("{scriptSrc}", scriptSrc);

                scriptsStr.append(macroResolver.resolveMacros(script));
                scriptsStr.append(System.getProperty("line.separator"));
			}
		}
		
		return scriptsStr.toString();
	}

}
