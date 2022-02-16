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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.security.authentication.AuthenticationManager;
import org.craftercms.security.exception.AuthenticationSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.beans.ConstructorProperties;

/**
 * Implementation of {@link LogoutHandler} for Profile
 *
 * @author joseross
 * @since 3.1.5
 */
public class ProfileLogoutHandler implements LogoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProfileLogoutHandler.class);

    protected AuthenticationManager profileAuthenticationManager;

    @ConstructorProperties({"profileAuthenticationManager"})
    public ProfileLogoutHandler(final AuthenticationManager profileAuthenticationManager) {
        this.profileAuthenticationManager = profileAuthenticationManager;
    }

    @Override
    public void logout(final HttpServletRequest request, final HttpServletResponse response,
                       final Authentication authentication) {
        if (authentication.getPrincipal() instanceof ProfileUser) {
            ProfileUser profileUser = (ProfileUser) authentication.getPrincipal();
            if (profileUser.authentication != null) {
                try {
                    profileAuthenticationManager.invalidateAuthentication(profileUser.authentication);
                } catch (AuthenticationSystemException e) {
                    logger.error("Error invalidating authentication for {}", profileUser.profile, e);
                }
            }
        }
    }

}
