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

package org.craftercms.engine.entitlement;

import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.model.Module;
import org.craftercms.commons.entitlements.usage.EntitlementUsageProvider;
import org.craftercms.engine.service.context.SiteContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link EntitlementUsageProvider} for Crafter Engine module.
 *
 * @author joseross
 */
public class EngineEntitlementUsageProvider implements EntitlementUsageProvider {

    /**
     * Current {@link SiteContextManager} instance.
     */
    protected SiteContextManager siteContextManager;

    @Autowired
    public void setSiteContextManager(@Lazy final SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Module getModule() {
        return Module.ENGINE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntitlementType> getSupportedEntitlements() {
        return Collections.singletonList(EntitlementType.SITE);
    }

    @Override
    public int doGetEntitlementUsage(final EntitlementType type) {
        return (int) siteContextManager.listContexts()
            .stream()
            .filter(context -> !context.isFallback())
            .count();
    }

}
