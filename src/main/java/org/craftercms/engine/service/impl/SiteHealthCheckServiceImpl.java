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
package org.craftercms.engine.service.impl;

import org.craftercms.engine.service.SiteHealthCheckService;
import org.craftercms.engine.service.context.SiteListResolver;
import org.craftercms.engine.service.health.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.util.Collection;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Default {@link SiteHealthCheckService} implementation.
 */
public class SiteHealthCheckServiceImpl implements SiteHealthCheckService {
    private static final Logger logger = LoggerFactory.getLogger(SiteHealthCheckServiceImpl.class);

    protected SiteListResolver siteListResolver;

    protected Collection<HealthCheck> healthChecks;

    @ConstructorProperties({"siteListResolver", "healthChecks"})
    public SiteHealthCheckServiceImpl(final SiteListResolver siteListResolver,
                                      final Collection<HealthCheck> healthChecks) {
        this.siteListResolver = siteListResolver;
        this.healthChecks = healthChecks;
    }

    @Override
    public boolean healthCheck() {
        logger.debug("Check health for all sites");
        Collection<String> siteNames = siteListResolver.getSiteList();

        return isEmpty(siteNames) || siteNames
                .stream()
                .anyMatch(this::healthCheck);
    }

    @Override
    public boolean healthCheck(final String site) {
        logger.debug("Checking health for site '{}'", site);
        return healthChecks.stream()
                .allMatch(checker -> checker.checkHealth(site));
    }
}
