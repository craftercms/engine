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

package org.craftercms.engine.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.processors.impl.AddSecurityCookiesProcessor;
import org.craftercms.security.utils.SecurityUtils;

/**
 * Extension of {@link org.craftercms.security.processors.impl.AddSecurityCookiesProcessor} that avoids adding
 * the cookies if the authentication is a {@link org.craftercms.engine.security.PersonaAuthentication}.
 *
 * @author avasquez
 */
public class PreviewAddSecurityCookiesProcessor extends AddSecurityCookiesProcessor {

    @Override
    protected AddSecurityCookiesResponseWrapper wrapResponse(RequestContext context) {
        return new PreviewAddSecurityCookiesResponseWrapper(context.getRequest(), context.getResponse());
    }

    protected class PreviewAddSecurityCookiesResponseWrapper extends AddSecurityCookiesResponseWrapper {

        public PreviewAddSecurityCookiesResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        @Override
        public void addCookies() {
            Authentication auth = SecurityUtils.getAuthentication(request);
            if (auth instanceof PersonaAuthentication) {
                // Delete cookies if they still exist from a previous authentication
                deleteTicketCookie();
                deleteProfileLastModifiedCookie();
            } else {
                super.addCookies();
            }
        }
    }

}
