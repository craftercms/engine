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

/**
 * Controller used to render status code errors like 404, 500, etc.
 *
 * @author Alfonso Vásquez
 */
@Controller
@RequestMapping(ErrorPageRenderController.URL_ROOT)
public class ErrorPageRenderController {

    public static final String URL_ROOT = "/crafter-controller/error";

    public static final String ERROR_CODE_PATH_VAR = "code";

    private String errorViewNamePrefix;

    @Required
    public void setErrorViewNamePrefix(String errorViewNamePrefix) {
        this.errorViewNamePrefix = errorViewNamePrefix;
    }

    @RequestMapping(value = "/{" + ERROR_CODE_PATH_VAR + "}")
    public String render(@PathVariable(ERROR_CODE_PATH_VAR) String code) {
        return errorViewNamePrefix + code + ".ftl";
    }

}
