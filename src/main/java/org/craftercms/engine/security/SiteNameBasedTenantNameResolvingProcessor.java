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
package org.craftercms.engine.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.craftercms.security.processors.RequestSecurityProcessor;
import org.craftercms.security.processors.RequestSecurityProcessorChain;
import org.craftercms.security.utils.SecurityUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Security processor that resolves the tenant using the site name request attribute set by the
 * {@link org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter}.
 *
 * @author Alfonso Vásquez
 */
public class SiteNameBasedTenantNameResolvingProcessor implements RequestSecurityProcessor {

    private static final Log logger = LogFactory.getLog(SiteNameBasedTenantNameResolvingProcessor.class);

    @Override
    public void processRequest(RequestContext context, RequestSecurityProcessorChain processorChain) throws Exception {
        HttpServletRequest request = context.getRequest();
        String tenant = (String) request.getAttribute(AbstractSiteContextResolvingFilter.SITE_NAME_ATTRIBUTE);

        if (logger.isDebugEnabled()) {
            logger.debug("Tenant resolved for current request: " + tenant);
        }

        SecurityUtils.setTenant(request, tenant);

        processorChain.processRequest(context);
    }

}
