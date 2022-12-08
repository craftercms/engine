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

package org.craftercms.engine.controller.rest;

import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.commons.monitoring.rest.MonitoringRestControllerBase;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.engine.util.logging.CircularQueueLogAppender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SITE_ID;

/**
 * Rest controller to provide monitoring information & site logs
 */
@RestController
@RequestMapping(MonitoringController.URL_ROOT)
public class MonitoringController extends MonitoringRestControllerBase {

    public final static String URL_ROOT = "/api/1";
    public final static String LOG_URL = "/log";

    private final String configuredToken;

    @ConstructorProperties({"configuredToken"})
    public MonitoringController(final String configuredToken) {
        this.configuredToken = configuredToken;
    }

    @GetMapping(MonitoringRestControllerBase.ROOT_URL + LOG_URL)
    @ValidateParams
    public List<Map<String, Object>> getLoggedEvents(@RequestParam @EsapiValidatedParam(maxLength = 50, type = SITE_ID) String site,
                                                     @RequestParam long since,
                                                     @RequestParam String token) throws InvalidManagementTokenException {
        validateToken(token);
        return CircularQueueLogAppender.getLoggedEvents(site, since);
    }

    @Override
    public String getConfiguredToken() {
        return configuredToken;
    }

}
