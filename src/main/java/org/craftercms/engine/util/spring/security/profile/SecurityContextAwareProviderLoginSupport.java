/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.util.spring.security.profile;

import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.exception.AuthenticationException;
import org.craftercms.security.social.impl.ProviderLoginSupportImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.social.connect.web.ConnectSupport;

/**
 * Wrapper for {@link ProviderLoginSupportImpl} that integrates the authenticated profile with Spring Security
 *
 * @author joseross
 * @since 3.1.5
 */
public class SecurityContextAwareProviderLoginSupport extends ProviderLoginSupportImpl {

    @Override
    public Authentication complete(final String tenant, final String providerId, final HttpServletRequest request,
                                   final Set<String> newUserRoles, final Map<String, Object> newUserAttributes,
                                   final ConnectSupport connectSupport) throws AuthenticationException {
        Authentication auth =
            super.complete(tenant, providerId, request, newUserRoles, newUserAttributes, connectSupport);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            securityContext = new SecurityContextImpl();
        }

        ProfileUser principal = new ProfileUser(auth);
        securityContext.setAuthentication(
            new PreAuthenticatedAuthenticationToken(principal, "N/A", principal.getAuthorities()));

        SecurityContextHolder.setContext(securityContext);

        return auth;
    }

}
