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
package org.craftercms.engine.util.spring;

import org.craftercms.engine.service.context.SiteContext;
import org.springframework.context.ApplicationContext;

/**
 * Bean that provide simple access to the Spring application context's beans: first it uses the site's own context.
 * If not specified, it uses the default context. Used for example to access Spring beans from an FTL or a Groovy
 * script.
 *
 * @author Alfonso Vásquez
 */
public class ApplicationContextAccessor extends org.craftercms.commons.spring.context.ApplicationContextAccessor {

    public ApplicationContextAccessor() {
    }

    public ApplicationContextAccessor(ApplicationContext actualApplicationContext) {
        super(actualApplicationContext);
    }

    @Override
    protected ApplicationContext getApplicationContext() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            ApplicationContext applicationContext = siteContext.getApplicationContext();
            if (applicationContext != null) {
                return applicationContext;
            }
        }

        return super.getApplicationContext();
    }

}
