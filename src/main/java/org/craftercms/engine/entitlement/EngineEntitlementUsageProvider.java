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

package org.craftercms.engine.entitlement;

import java.util.Collections;
import java.util.List;

import org.craftercms.commons.entitlements.model.Entitlement;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.model.Module;
import org.craftercms.commons.entitlements.usage.EntitlementUsageProvider;
import org.craftercms.engine.service.context.SiteContextManager;
import org.springframework.beans.factory.annotation.Required;

import static org.craftercms.commons.entitlements.model.Module.ENGINE;

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

    @Required
    public void setSiteContextManager(final SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Module getModule() {
        return ENGINE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entitlement> getCurrentUsage() {
        Entitlement sites = new Entitlement();
        int totalSites = (int) siteContextManager.listContexts()
                                            .stream()
                                            .filter(context -> !context.isFallback())
                                            .count();

        sites.setType(EntitlementType.SITE);
        sites.setValue(totalSites);

        return Collections.singletonList(sites);
    }

}
