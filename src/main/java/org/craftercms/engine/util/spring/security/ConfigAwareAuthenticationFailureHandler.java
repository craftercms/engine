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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

/**
 * Extension of {@link SimpleUrlAuthenticationFailureHandler} that uses site config to override properties
 *
 * @author joseross
 * @since 3.1.5
 */
public class ConfigAwareAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public static final String LOGIN_FAILURE_URL_KEY = "security.login.failureUrl";

    protected String defaultFailureUrl;

    protected String determineFailureUrl() {
        HierarchicalConfiguration siteConfig = ConfigUtils.getCurrentConfig();
        if (siteConfig != null && siteConfig.containsKey(LOGIN_FAILURE_URL_KEY)) {
            return siteConfig.getString(LOGIN_FAILURE_URL_KEY);
        }
        return defaultFailureUrl;
    }

    // This was needed because the super class doesn't use getter for the url :(

    public void setDefaultFailureUrl(String defaultFailureUrl) {
        Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), "'"
            + defaultFailureUrl + "' is not a valid redirect URL");
        this.defaultFailureUrl = defaultFailureUrl;
    }

    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response, AuthenticationException exception)
        throws IOException, ServletException {

        String failureUrl = determineFailureUrl();
        if (failureUrl == null) {
            logger.debug("No failure URL set, sending 401 Unauthorized error");

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication Failed: " + exception.getMessage());
        }
        else {
            saveException(request, exception);

            if (isUseForward()) {
                logger.debug("Forwarding to " + failureUrl);

                request.getRequestDispatcher(failureUrl)
                    .forward(request, response);
            }
            else {
                logger.debug("Redirecting to " + failureUrl);
                getRedirectStrategy().sendRedirect(request, response, failureUrl);
            }
        }
    }

}
