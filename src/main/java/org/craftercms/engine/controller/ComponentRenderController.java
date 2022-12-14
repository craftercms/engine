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
package org.craftercms.engine.controller;

import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.beans.ConstructorProperties;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.HTTPURI;

/**
 * @author Alfonso Vásquez
 */
@Controller
@RequestMapping(ComponentRenderController.URL_ROOT)
public class ComponentRenderController {

    public static final String URL_ROOT = "/crafter-controller/component";

    public final String COMPONENT_PATH_MODEL_NAME = "componentPath";

    private final String renderComponentViewName;

    @ConstructorProperties({"renderComponentViewName"})
    public ComponentRenderController(String renderComponentViewName) {
        this.renderComponentViewName = renderComponentViewName;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    @ValidateParams
    protected ModelAndView render(@ValidateSecurePathParam
                                  @EsapiValidatedParam(maxLength = 4000, type = HTTPURI)
                                  @RequestParam("path") String path) throws Exception {
        return new ModelAndView(renderComponentViewName, COMPONENT_PATH_MODEL_NAME, path);
    }

}
