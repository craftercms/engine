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
package org.craftercms.engine.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.craftercms.engine.http.impl.DefaultExceptionHandler;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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

    public static final String STACK_TRACE_ATTRIBUTE = "stackTrace";

    private String errorViewNamePrefix;

    @Required
    public void setErrorViewNamePrefix(String errorViewNamePrefix) {
        this.errorViewNamePrefix = errorViewNamePrefix;
    }

    @RequestMapping(value = "/{" + ERROR_CODE_PATH_VAR + "}")
    public ModelAndView render(@PathVariable(ERROR_CODE_PATH_VAR) String code, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName(errorViewNamePrefix + code + ".ftl");

        Exception error = (Exception)request.getAttribute(DefaultExceptionHandler.EXCEPTION_ATTRIBUTE);
        if (error != null) {
            StringWriter errorWriter = new StringWriter();

            try {
                error.printStackTrace(new PrintWriter(errorWriter));
            } finally {
                IOUtils.closeQuietly(errorWriter);
            }

            mv.addObject(STACK_TRACE_ATTRIBUTE, errorWriter.toString());
        }

        return mv;
    }

}
