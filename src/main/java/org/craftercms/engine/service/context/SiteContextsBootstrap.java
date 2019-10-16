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

package org.craftercms.engine.service.context;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Class that is used to create all contexts after Spring startup (if the {@code createContextsOnStartup} is true).
 *
 * @author avasquez
 */
public class SiteContextsBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    protected boolean createContextsOnStartup;
    protected SiteListResolver siteListResolver;
    protected SiteContextManager siteContextManager;

    protected boolean triggered;

    @Required
    public void setCreateContextsOnStartup(boolean createContextsOnStartup) {
        this.createContextsOnStartup = createContextsOnStartup;
    }

    @Required
    public void setSiteListResolver(SiteListResolver siteListResolver) {
        this.siteListResolver = siteListResolver;
    }

    @Required
    public void setSiteContextManager(SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!triggered && createContextsOnStartup) {
            triggered = true;

            siteContextManager.createContexts(siteListResolver.getSiteList());
        }
    }

}
