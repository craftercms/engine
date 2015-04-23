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
package org.craftercms.engine.service.context;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Registry for site {@link SiteContext}s.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextRegistry {

    private static final Log logger = LogFactory.getLog(SiteContextRegistry.class);

    protected Map<String, SiteContext> registry;

    public SiteContextRegistry() {
        registry = new ConcurrentHashMap<>();
    }

    @PreDestroy
    public void destroy() {
        for (Iterator<SiteContext> iter = registry.values().iterator(); iter.hasNext();) {
            iter.next().destroy();
            iter.remove();
        }

        logger.info("Site context registry destroyed");
    }

    public Collection<SiteContext> list() {
        return registry.values();
    }

    public SiteContext get(String siteName) {
        return registry.get(siteName);
    }

    public void register(SiteContext context) {
        registry.put(context.getSiteName(), context);

        logger.info("Site context registered: " + context);
    }

    public SiteContext unregister(String siteName) {
        SiteContext context = registry.remove(siteName);
        if (context != null) {
            context.destroy();

            logger.info("Site context unregistered: " + context);
        }

        return null;
    }

}
