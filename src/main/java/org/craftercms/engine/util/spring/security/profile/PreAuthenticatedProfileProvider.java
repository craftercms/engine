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

package org.craftercms.engine.util.spring.security.profile;

import org.craftercms.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Implementation of {@link AuthenticationProvider} that handles pre-authenticated profiles
 *
 * @author joseross
 * @since 3.1.5
 */
public class PreAuthenticatedProfileProvider implements AuthenticationProvider {

    protected AuthenticationManager authenticationManager;

    public PreAuthenticatedProfileProvider(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (authentication.getPrincipal() instanceof ProfileUser) {
            ProfileUser principal = (ProfileUser) authentication.getPrincipal();
            return new PreAuthenticatedAuthenticationToken(
                new ProfileUser(authenticationManager.authenticateUser(principal.getProfile())),
                authentication.getCredentials(),
                principal.getAuthorities());
        }
        return null;
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
