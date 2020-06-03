/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.servlet;

import org.mitre.dsmiley.httpproxy.ProxyServlet;

/**
 * Extension of {@link ProxyServlet} that uses the current site configuration
 *
 * @author joseross
 * @since 3.1.7
 */
public class ConfigAwareProxyServlet extends ProxyServlet {

    // Expose protected constants
    public static final String ATTR_TARGET_URI = ProxyServlet.ATTR_TARGET_URI;
    public static final String ATTR_TARGET_HOST = ProxyServlet.ATTR_TARGET_HOST;

    @Override
    protected void initTarget() {
        // Do nothing ... the target url will be resolved for each request
    }

}
