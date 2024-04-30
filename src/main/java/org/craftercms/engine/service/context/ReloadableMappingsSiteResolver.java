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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.CrafterException;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.InitializingBean;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves the current site name from a mapping
 * of the request domain name to site name. These mappings are dynamic in that anytime while Engine is running they
 * can change (ones can be added and others removed). The {@link #reloadMappings()} method then can be called to
 * reload the mappings.
 *
 * @author avasquez
 */
public class ReloadableMappingsSiteResolver implements SiteListResolver, SiteResolver, InitializingBean {

    private static final Log logger = LogFactory.getLog(ReloadableMappingsSiteResolver.class);

    protected Resource mappingsFile;
    protected SiteContextManager siteContextManager;

    protected volatile Properties mappings;

    public ReloadableMappingsSiteResolver(Resource mappingsFile, SiteContextManager siteContextManager) {
        this.mappingsFile = mappingsFile;
        this.siteContextManager = siteContextManager;
    }

    public void afterPropertiesSet() throws Exception {
        loadMappings();
    }

    public synchronized void reloadMappings() throws CrafterException {
        loadMappings();

        siteContextManager.syncContexts();
    }

    @Override
    public Collection<String> getSiteList() {
        Collection<Object> siteNames = mappings.values();
        Set<String> result = new LinkedHashSet<>(siteNames.size());

        for (Object siteName : siteNames) {
            result.add(siteName.toString());
        }

        return result;
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        String domainName = request.getServerName();

        if (mappings.containsKey(domainName)) {
            return (String)mappings.get(domainName);
        } else {
            if (logger.isDebugEnabled()) {
                logger.warn("No site mapping found for domain name " + domainName);
            }

            return null;
        }
    }

    protected void loadMappings() throws CrafterException {
        Properties tmpMappings = new Properties();

        try {
            tmpMappings.load(mappingsFile.getInputStream());
        } catch (IOException e) {
            throw new CrafterException("Unable to load domain name to site name mappings from " + mappingsFile, e);
        }

        Properties newMappings = new Properties();

        for (Map.Entry<Object, Object> entry : tmpMappings.entrySet()) {
            String trimmedKey = entry.getKey().toString().trim();
            String trimmedVal = entry.getValue().toString().trim();

            newMappings.setProperty(trimmedKey, trimmedVal);
        }

        logger.info("Domain name to site name mappings loaded from " + mappingsFile);

        mappings = newMappings;
    }

}
