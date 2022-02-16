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

import java.beans.ConstructorProperties;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.profile.api.PersistentLogin;
import org.craftercms.profile.api.exceptions.ProfileException;
import org.craftercms.profile.api.services.AuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Implementation of {@link AbstractRememberMeServices} for Profile
 *
 * @author joseross
 * @since 3.1.5
 */
public class ProfileRememberMeServices extends AbstractRememberMeServices {

    protected AuthenticationService authenticationService;

    @ConstructorProperties({"key", "userDetailsService", "authenticationService"})
    public ProfileRememberMeServices(final String key, final UserDetailsService userDetailsService,
                                     final AuthenticationService authenticationService) {
        super(key, userDetailsService);
        this.authenticationService = authenticationService;
    }

    @Override
    protected void onLoginSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                  final Authentication successfulAuthentication) {
        ProfileUser profileUser = (ProfileUser) successfulAuthentication.getPrincipal();
        try {
            PersistentLogin persistentLogin =
                authenticationService.createPersistentLogin(profileUser.getProfile().getId().toHexString());
            setCookie(new String[]{ persistentLogin.getId(), persistentLogin.getToken() }, getTokenValiditySeconds(),
                request, response);
        } catch (ProfileException e) {
            throw new RememberMeAuthenticationException(
                "Error creating persistent login for " + profileUser.getUsername(), e);
        }
    }

    @Override
    public void logout(final HttpServletRequest request, final HttpServletResponse response,
                       final Authentication authentication) {
        super.logout(request, response, authentication);

        // Needed because the super class expects to work only with the username
        String cookie = extractRememberMeCookie(request);
        if (authentication != null && isNotEmpty(cookie)) {
            String persistentLoginId = decodeCookie(cookie)[0];
            try {
                authenticationService.deletePersistentLogin(persistentLoginId);
            } catch (ProfileException e) {
                throw new RememberMeAuthenticationException(
                    "Error deleting persistent login " + persistentLoginId, e);
            }
        }
    }

    @Override
    protected UserDetails processAutoLoginCookie(final String[] cookieTokens, final HttpServletRequest request,
                                                 final HttpServletResponse response)
        throws RememberMeAuthenticationException, UsernameNotFoundException {

        if (cookieTokens.length != 2) {
            throw new InvalidCookieException(
                "Cookie token did not contain 2 tokens, but contained '" + Arrays.asList(cookieTokens) + "'");
        }

        final String presentedId = cookieTokens[0];
        final String presentedToken = cookieTokens[1];

        try {
            PersistentLogin persistentLogin = authenticationService.getPersistentLogin(presentedId);

            if (persistentLogin == null) {
                // No series match, so we can't authenticate using this cookie
                throw new RememberMeAuthenticationException(
                    "No persistent token found for id: " + presentedId);
            }

            // We have a match for this user/series combination
            if (!presentedToken.equals(persistentLogin.getToken())) {
                // Token doesn't match series value. Delete all logins for this user and throw
                // an exception to warn them.
                authenticationService.deletePersistentLogin(presentedId);

                throw new CookieTheftException(
                    "Invalid remember-me token (id/token) mismatch. Implies previous cookie theft attack.");
            }

            if (persistentLogin.getTimestamp().getTime() + getTokenValiditySeconds() * 1000L < currentTimeMillis()) {
                throw new RememberMeAuthenticationException("Remember-me login has expired");
            }

            // Token also matches, so login is valid. Update the token value, keeping the
            // *same* series number.
            if (logger.isDebugEnabled()) {
                logger.debug("Refreshing persistent login token for profile '"
                    + persistentLogin.getProfileId() + "', id '" + persistentLogin.getId() + "'");
            }

            persistentLogin = authenticationService.refreshPersistentLoginToken(presentedId);

            setCookie(new String[]{ persistentLogin.getId(), persistentLogin.getToken() }, getTokenValiditySeconds(),
                request, response);

            return ((ProfileUserDetailsService) getUserDetailsService()).loadUserById(persistentLogin.getProfileId());

        } catch (ProfileException e) {
            throw new RememberMeAuthenticationException("Error validating persistent login " + presentedId, e);
        }
    }

}
