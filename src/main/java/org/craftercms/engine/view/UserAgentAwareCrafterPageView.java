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

package org.craftercms.engine.view;

import org.craftercms.engine.exception.RenderingException;
import org.craftercms.engine.mobile.UserAgentTemplateDetector;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.Script;
import org.springframework.web.servlet.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ViewResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserAgentAwareCrafterPageView extends CrafterPageView {

    protected UserAgentTemplateDetector userAgentTemplateDetector;

    public UserAgentAwareCrafterPageView(SiteItem page, Locale locale, String pageViewNameXPathQuery, String mimeTypeXPathQuery,
                                         List<Script> scripts, ViewResolver delegatedViewResolver,
                                         UserAgentTemplateDetector userAgentTemplateDetector) {
        super(page, locale, pageViewNameXPathQuery, mimeTypeXPathQuery, scripts, delegatedViewResolver);
        this.userAgentTemplateDetector = userAgentTemplateDetector;
    }

    @Override
    protected void renderActualView(String pageViewName, Map<String, Object> model, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        String userAgentSpecificPageViewName = userAgentTemplateDetector.resolveAgentTemplate(request, pageViewName);
        View actualView = delegatedViewResolver.resolveViewName(userAgentSpecificPageViewName, locale);
        if (actualView == null) {
            actualView = delegatedViewResolver.resolveViewName(pageViewName, locale);
            if (actualView == null) {
                throw new RenderingException("No view was resolved for page view name '" + pageViewName + "'");
            }
        }

        actualView.render(model, request, response);
    }

}
