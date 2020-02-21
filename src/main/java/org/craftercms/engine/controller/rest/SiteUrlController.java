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

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.UrlTransformationService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller to access the URL transformation service.
 *
 * @author joseross
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteUrlController.URL_ROOT)
public class SiteUrlController extends RestControllerBase {

    public static final String URL_ROOT = "/site/url";
    public static final String URL_TRANSFORM = "/transform";

    protected UrlTransformationService urlTransformationService;

    @Required
    public void setUrlTransformationService(final UrlTransformationService urlTransformationService) {
        this.urlTransformationService = urlTransformationService;
    }

    @GetMapping(URL_TRANSFORM)
    public String transformUrl(@RequestParam String transformerName, @RequestParam String url) {
        return urlTransformationService.transform(transformerName, url);
    }

}
