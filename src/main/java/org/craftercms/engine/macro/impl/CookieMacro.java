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
package org.craftercms.engine.macro.impl;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.commons.http.RequestContext;
import org.springframework.beans.factory.annotation.Required;

/**
 * Represents a macro that can be a cookie value.
 *
 * @author Alfonso Vásquez
 */
public class CookieMacro extends AbstractMacro {

    private static final Log logger = LogFactory.getLog(CookieMacro.class);

    private String cookieName;

    @Required
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    protected String createMacroName() {
        return "{" + cookieName + "}";
    }

    @Override
    protected String getMacroValue(String str) {
        RequestContext requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            Cookie cookie = HttpUtils.getCookie(cookieName, requestContext.getRequest());
            if (cookie != null) {
                return cookie.getValue();
            }
        }

        return null;
    }

}
