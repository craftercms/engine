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

package org.craftercms.engine.view;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.engine.exception.RenderingException;
import org.craftercms.engine.mobile.UserAgentTemplateDetector;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.View;

public class UserAgentAwareCrafterPageView extends CrafterPageView {

    protected UserAgentTemplateDetector userAgentTemplateDetector;

    @Required
    public void setUserAgentTemplateDetector(UserAgentTemplateDetector userAgentTemplateDetector) {
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
