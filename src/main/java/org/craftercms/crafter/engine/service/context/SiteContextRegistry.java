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
package org.craftercms.crafter.engine.service.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for site {@link SiteContext}s.
 *
 * @author Alfonso Vásquez
 */
public class SiteContextRegistry implements BeanPostProcessor {

    private static final Log logger = LogFactory.getLog(SiteContextRegistry.class);

    protected Map<String, SiteContext> registry;

    public SiteContextRegistry() {
        registry = new ConcurrentHashMap<String, SiteContext>();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SiteContext) {
            register((SiteContext) bean);
        }

        return bean;
    }

    public SiteContext get(String siteName) {
        return registry.get(siteName);
    }

    public void register(SiteContext siteContext) {
        registry.put(siteContext.getSiteName(), siteContext);

        logger.info("[SITE CONTEXT REGISTRY] Site context registered: " + siteContext);
    }

    public SiteContext unregister(String siteName) {
        SiteContext siteContext = registry.remove(siteName);

        logger.info("[SITE CONTEXT REGISTRY] Site context unregistered: " + siteContext);

        return siteContext;
    }

    public Collection<SiteContext> getSiteContexts() {
        return registry.values();
    }

}
