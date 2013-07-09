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
package org.craftercms.crafter.engine.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.crafter.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default controller for rendering Crafter pages. If the site context is the fallback context, a fallback page is always rendered.
 *
 * @author Alfonso Vasquez
 * @author Dejan Brkic
 */
public class PageRenderController extends AbstractController {
	
	private static final Log logger = LogFactory.getLog(PageRenderController.class);

    protected String fallbackPageUrl;

    @Required
    public void setFallbackPageUrl(String fallbackPageUrl) {
        this.fallbackPageUrl = fallbackPageUrl;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String pageUrl;

        if (AbstractSiteContextResolvingFilter.getCurrentContext().isFallback()) {
            logger.warn("Rendering fallback page [" + fallbackPageUrl + "]");

            pageUrl = fallbackPageUrl;
        } else {
            pageUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            if (StringUtils.isEmpty(pageUrl)) {
                throw new IllegalStateException("Required request attribute '" + HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE +
                        "' is not set");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Rendering page [" + pageUrl + "]");
            }
        }

        return new ModelAndView(pageUrl);
    }

}
