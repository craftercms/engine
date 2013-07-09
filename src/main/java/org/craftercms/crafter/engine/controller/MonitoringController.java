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
package org.craftercms.crafter.engine.controller;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(MonitoringController.URL_ROOT)
public class MonitoringController {

    public final static String URL_ROOT = "/api/1/monitoring";

    public static final String ACTION_PATH_VAR = "action";

    private String statusViewNamePrefix;

    @Required
    public void setStatusViewNamePrefix(String statusViewNamePrefix) {
        this.statusViewNamePrefix = statusViewNamePrefix;
    }

    @RequestMapping(value = "/{" + ACTION_PATH_VAR + "}", method = RequestMethod.GET)
    public String render(@PathVariable(ACTION_PATH_VAR) String action) {
        return statusViewNamePrefix + action + ".ftl";
    }
}
