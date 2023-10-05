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
package org.craftercms.engine.controller;

import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.beans.ConstructorProperties;


/**
 * @author Alfonso Vásquez
 */
@Controller
@Validated
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
    protected ModelAndView render(@ValidExistingContentPath
                                  @RequestParam("path") String path) throws Exception {
        return new ModelAndView(renderComponentViewName, COMPONENT_PATH_MODEL_NAME, path);
    }

}
