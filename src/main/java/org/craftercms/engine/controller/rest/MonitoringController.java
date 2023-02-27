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

package org.craftercms.engine.controller.rest;

import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.engine.util.logging.CircularQueueLogAppender;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

/**
 * Rest controller to provide monitoring information & site logs
 */
@Validated
@RestController
@RequestMapping(MonitoringController.URL_ROOT)
public class MonitoringController extends MonitoringRestControllerBase {

    public final static String URL_ROOT = "/api/1";
    public final static String LOG_URL = "/log";


    @ConstructorProperties({"configuredToken"})
    public MonitoringController(final String configuredToken) {
        super(configuredToken);
    }

    @GetMapping(MonitoringRestControllerBase.ROOT_URL + LOG_URL)
    public List<Map<String, Object>> getLoggedEvents(@RequestParam @ValidSiteId String site,
                                                     @Positive @RequestParam long since,
                                                     @RequestParam String token) throws InvalidManagementTokenException {
        validateToken(token);
        return CircularQueueLogAppender.getLoggedEvents(site, since);
    }

}
