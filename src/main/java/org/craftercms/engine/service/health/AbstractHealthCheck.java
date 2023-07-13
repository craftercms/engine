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
package org.craftercms.engine.service.health;

import org.craftercms.engine.service.context.SiteContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of {@link HealthCheck} with logging.
 */
public abstract class AbstractHealthCheck implements HealthCheck {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final SiteContextManager contextManager;

    public AbstractHealthCheck(final SiteContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public boolean checkHealth(String site) {
        logger.debug("Check health for site: '{}'", site);
        try {
            boolean isValid = doCheckHealth(site);
            logger.debug("Check health passed for site: '{}': {}", site, isValid);
            return isValid;
        } catch (Exception e) {
            logger.warn("Check health failed for site: '{}'", site, e);
            return false;
        }
    }

    /**
     * Performs the actual health check.
     *
     * @param site the site to check.
     * @return true if the site is healthy, false otherwise.
     */
    protected abstract boolean doCheckHealth(String site);

}
