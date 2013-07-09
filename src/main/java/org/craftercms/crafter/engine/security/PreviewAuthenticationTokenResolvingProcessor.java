/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.crafter.engine.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.crafter.core.util.HttpServletUtils;
import org.craftercms.crafter.security.api.RequestContext;
import org.craftercms.crafter.security.api.RequestSecurityProcessor;
import org.craftercms.crafter.security.api.RequestSecurityProcessorChain;
import org.craftercms.crafter.security.api.UserProfile;
import org.craftercms.crafter.security.authentication.AuthenticationToken;
import org.craftercms.crafter.security.utils.SecurityUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.crafter.core.util.HttpServletUtils.SCOPE_SESSION;
import static org.craftercms.crafter.engine.controller.preview.rest.ProfileRestController.PROFILE_SESSION_ATTRIBUTE;

/**
 * Obtains and sets the authentication token for the current request, using the current Crafter Studio persona.
 *
 * @author Alfonso Vásquez
 */
public class PreviewAuthenticationTokenResolvingProcessor implements RequestSecurityProcessor {

    private static final Log logger = LogFactory.getLog(RequestSecurityProcessor.class);

    @Override
    public void processRequest(RequestContext context, RequestSecurityProcessorChain processorChain) throws Exception {
        UserProfile profile;
        Map<String, Object> attributes = (Map<String, Object>) HttpServletUtils.getAttribute(PROFILE_SESSION_ATTRIBUTE, SCOPE_SESSION);

        if (attributes != null) {
            profile = new UserProfile();
            profile.setUserName((String) attributes.get("username"));
            profile.setId((String) attributes.get("username"));
            profile.setActive(true);

            Map<String, Object> attributesWithoutUsername = new HashMap<String, Object>(attributes);
            attributesWithoutUsername.remove("username");

            profile.setAttributes(attributesWithoutUsername);

            String rolesStr = (String) attributes.get("roles");
            if (rolesStr != null) {
                String[] roles = rolesStr.split(",");
                profile.getRoles().addAll(Arrays.asList(roles));
            }
        } else {
            profile = SecurityUtils.getAnonymousProfile();
        }

        AuthenticationToken token = new AuthenticationToken();
        token.setProfile(profile);

        context.setAuthenticationToken(token);

        if (logger.isDebugEnabled()) {
            logger.debug("Authentication token for request '" + context.getRequestUri() + "': " + token);
        }

        processorChain.processRequest(context);
    }

}
