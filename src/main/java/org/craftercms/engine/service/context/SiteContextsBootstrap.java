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

package org.craftercms.engine.service.context;

import org.craftercms.engine.event.SiteContextsBootstrappedEvent;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Class that is used to create all contexts after Spring startup (if the {@code createContextsOnStartup} is true).
 *
 * @author avasquez
 */
public class SiteContextsBootstrap implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    protected ApplicationContext applicationContext;
    protected boolean createContextsOnStartup;
    protected boolean createConcurrently;
    protected SiteContextManager siteContextManager;

    protected boolean triggered;

    public SiteContextsBootstrap() {
        createConcurrently = false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Required
    public void setCreateContextsOnStartup(boolean createContextsOnStartup) {
        this.createContextsOnStartup = createContextsOnStartup;
    }

    public void setCreateConcurrently(boolean createConcurrently) {
        this.createConcurrently = createConcurrently;
    }

    @Required
    public void setSiteContextManager(SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!triggered && createContextsOnStartup) {
            triggered = true;

            siteContextManager.createContexts(createConcurrently);

            applicationContext.publishEvent(new SiteContextsBootstrappedEvent(this));
        }
    }
}
