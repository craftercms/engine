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
package org.craftercms.engine.service;

/**
 * Service to provide health check status.
 */
public interface SiteHealthCheckService {

    /**
     * Determine if current engine has valid healthy sites
     * Engine has valid healthy sites when:
     * 1. There are no sites at all
     * 2. There is one or more valid healthy site
     *
     * @return true if engine has healthy sites, false otherwise
     */
    boolean healthCheck();

    /**
     * Performs the health checks for the given site.
     *
     * @param site the site to check.
     * @return true if all the health checks pass, false otherwise.
     */
    boolean healthCheck(String site);
}
