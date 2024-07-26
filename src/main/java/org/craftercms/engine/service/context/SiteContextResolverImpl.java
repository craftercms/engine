/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.service.context;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Default {@link org.craftercms.engine.service.context.SiteContextResolver}. It uses a {@link SiteListResolver} to
 * resolve the current site name.The site name is later used to look for the context in the
 * {@link SiteContextManager} (or create it if it still doesn't exist). If no particular site name is resolved, then
 * a fallback site context will be used.
 *
 * @author avasquez
 */
public class SiteContextResolverImpl implements SiteContextResolver {

    public static final String SITE_NAME_ATTRIBUTE = "siteName";

    private static final Log logger = LogFactory.getLog(SiteContextResolverImpl.class);

    protected SiteResolver siteResolver;
    protected SiteContextManager siteContextManager;
    protected String fallbackSiteName;

    public SiteContextResolverImpl(SiteResolver siteResolver, String fallbackSiteName) {
        this.siteResolver = siteResolver;
        this.fallbackSiteName = fallbackSiteName;
    }

    @Autowired
    public void setSiteContextManager(@Lazy SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    @Override
    public SiteContext getContext(HttpServletRequest request) {
        boolean fallback = false;
        String siteName = StringUtils.lowerCase(siteResolver.getSiteName(request));

        if (StringUtils.isNotEmpty(siteName)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Site name resolved for current request: '" + siteName + "'");
            }
        } else {
            fallback = true;
            siteName = fallbackSiteName;

            if (logger.isDebugEnabled()) {
                logger.debug("Unable to resolve a site name for the request. Using fallback site");
            }
        }

        SiteContext siteContext = getContext(siteName, fallback);
        if (siteContext == null) {
            throw new IllegalStateException("Unable to resolve context for site name '" + siteName + "'");
        }

        request.setAttribute(SITE_NAME_ATTRIBUTE, siteName);

        return siteContext;
    }

    protected SiteContext getContext(String siteName, boolean fallback) {
        SiteContext siteContext = siteContextManager.getContext(siteName, fallback);

        if (logger.isDebugEnabled()) {
            logger.debug("Site context resolved for current request: " + siteContext);
        }

        return siteContext;
    }

}
