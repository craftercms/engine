/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.controller.rest;

import java.util.Map;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Collections.singletonMap;


/**
 * Controller to expose general configurations
 *
 * @author joseross
 * @since 3.1.1
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + ConfigRestController.URL_ROOT)
public class ConfigRestController {

    public static final String URL_ROOT = "/config";
    public static final String URL_MODE_PREVIEW = "/preview";

    protected boolean modePreview;

    @Required
    public void setModePreview(final boolean modePreview) {
        this.modePreview = modePreview;
    }

    /**
     * Indicates if the system is currently configured for preview
     */
    @GetMapping(URL_MODE_PREVIEW)
    public Map<String, Boolean> getModePreview() {
        return singletonMap("preview", modePreview);
    }

}
