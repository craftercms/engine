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

package org.craftercms.engine.util.spring.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;

/**
 * Extension of {@link PreAuthenticatedGrantedAuthoritiesUserDetailsService} that always returns the same principal
 * obtained from the authentication filter
 *
 * @author joseross
 * @since 3.1.5
 */
public class DefaultPreAuthenticatedUserDetailsService extends PreAuthenticatedGrantedAuthoritiesUserDetailsService {

    @Override
    protected UserDetails createUserDetails(final Authentication token,
                                            final Collection<? extends GrantedAuthority> authorities) {
        return (UserDetails) token.getPrincipal();
    }

}
