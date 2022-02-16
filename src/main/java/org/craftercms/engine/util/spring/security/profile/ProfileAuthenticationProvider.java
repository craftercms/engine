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

import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.authentication.AuthenticationManager;
import org.craftercms.security.utils.tenant.TenantsResolver;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.beans.ConstructorProperties;

/**
 * Implementation of {@link AbstractUserDetailsAuthenticationProvider} that handles form login for with Profile
 *
 * @author joseross
 * @since 3.1.5
 */
public class ProfileAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    protected TenantsResolver tenantsResolver;

    protected AuthenticationManager authenticationManager;

    @ConstructorProperties({"tenantsResolver", "authenticationManager"})
    public ProfileAuthenticationProvider(final TenantsResolver tenantsResolver,
                                         final AuthenticationManager authenticationManager) {
        this.tenantsResolver = tenantsResolver;
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void additionalAuthenticationChecks(final UserDetails userDetails,
                                                  final UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        // do nothing
    }

    @Override
    protected UserDetails retrieveUser(final String username,
                                       final UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        String[] tenants = getTenants();
        try {
            Authentication profileAuth =
                authenticationManager.authenticateUser(tenants, username, authentication.getCredentials().toString());

            return createPrincipal(profileAuth);
        } catch (Exception e) {
            throw new AuthenticationServiceException("Error authenticating user " + username, e);
        }
    }

    protected String[] getTenants() {
        String[] tenants = tenantsResolver.getTenants();
        if (ArrayUtils.isEmpty(tenants)) {
            throw new AuthenticationServiceException("No tenants resolved for authentication");
        }
        return tenants;
    }

    protected ProfileUser createPrincipal(Authentication auth) {
        return new ProfileUser(auth);
    }

}
