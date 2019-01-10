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
package org.craftercms.engine.freemarker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.craftercms.commons.http.RequestContext;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link TemplateExceptionHandler} that instead of printing the errors directly in the HTML and stopping template processing, stores
 * them in a model variable so they can be displayed later.
 *
 * @author Alfonso Vásquez
 */
public class CrafterTemplateExceptionHandler implements TemplateExceptionHandler {

    public static final String FREEMARKER_CURRENT_ERROR_ID_ATTRIBUTE = "freemarkerCurrentErrorId";

    public static final String ERROR_FORMAT =
        "<script type='text/javascript'>" +
            "function showError{errorId}() {" +
                "document.getElementById('error{errorId}').style.display = 'block';" +
                "document.getElementById('toggleError{errorId}Btn').innerHTML = 'Hide error';" +
            "}" +
            "function hideError{errorId}() {" +
                "document.getElementById('error{errorId}').style.display = 'none';" +
                "document.getElementById('toggleError{errorId}Btn').innerHTML = 'Show error';" +
            "}" +
            "function toggleError{errorId}() {" +
                "if (document.getElementById('error{errorId}').style.display == 'none') {" +
                    "showError{errorId}();" +
                "} else {" +
                    "hideError{errorId}();" +
                "}" +
            "}" +
        "</script>" +
        "<a id='toggleError{errorId}Btn' onclick='toggleError{errorId}()' style='color: red; font-size: 14px; " +
                "font-family: Arial, Helvetica, sans-serif; font-style: normal; font-variant: normal; font-weight: normal; " +
                "text-decoration: underline; text-transform: none; cursor: pointer'>Show error</a>" +
        "<div id='error{errorId}' style='display: none;'><pre>${error}</pre></div>";

    private boolean displayTemplateExceptionsInView;

    @Required
    public void setDisplayTemplateExceptionsInView(boolean displayTemplateExceptionsInView) {
        this.displayTemplateExceptionsInView = displayTemplateExceptionsInView;
    }

    @Override
    public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
        if (displayTemplateExceptionsInView) {
            String error = ERROR_FORMAT.replace("{errorId}", createErrorId());
            error = error.replace("{error}", getExceptionStackTrace(te));

            try {
                out.write(error);
            } catch (IOException e) {
                throw new TemplateException("Failed to print error. Cause: " + e, env);
            }
        }
    }

    protected String getExceptionStackTrace(TemplateException te) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        te.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }

    protected String createErrorId() {
        HttpServletRequest request = RequestContext.getCurrent().getRequest();
        Integer currentErrorId = (Integer)request.getAttribute(FREEMARKER_CURRENT_ERROR_ID_ATTRIBUTE);

        if (currentErrorId == null) {
            currentErrorId = 1;
        } else {
            currentErrorId++;
        }

        request.setAttribute(FREEMARKER_CURRENT_ERROR_ID_ATTRIBUTE, currentErrorId);

        return currentErrorId.toString();
    }

}
