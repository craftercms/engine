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
package org.craftercms.engine.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.controller.preview.rest.ProfileRestController;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.impl.DefaultAuthentication;
import org.craftercms.security.processors.RequestSecurityProcessor;
import org.craftercms.security.processors.RequestSecurityProcessorChain;
import org.craftercms.security.utils.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Obtains and sets the authentication for the current request, using the current Crafter Studio persona.
 *
 * @author Alfonso Vásquez
 */
public class PreviewCurrentAuthenticationResolvingProcessor implements RequestSecurityProcessor {

    private static final Log logger = LogFactory.getLog(RequestSecurityProcessor.class);

    @Override
    public void processRequest(RequestContext context, RequestSecurityProcessorChain processorChain) throws Exception {
        HttpServletRequest request = context.getRequest();
        Map<String, Object> attributes = (Map<String, Object>) request.getAttribute(ProfileRestController
                .PROFILE_SESSION_ATTRIBUTE);

        if (attributes != null && !"anonymous".equalsIgnoreCase((String) attributes.get("username"))) {
            Profile profile = new Profile();
            profile.setId(new ObjectId());
            profile.setUsername((String) attributes.get("username"));
            profile.setEnabled(true);

            String rolesStr = (String) attributes.get("roles");
            if (rolesStr != null) {
                String[] roles = rolesStr.split(",");
                profile.getRoles().addAll(Arrays.asList(roles));
            }

            Map<String, Object> attributesNoUsernameNoRoles = new HashMap<String, Object>(attributes);
            attributesNoUsernameNoRoles.remove("username");
            attributesNoUsernameNoRoles.remove("roles");

            profile.setAttributes(attributesNoUsernameNoRoles);

            SecurityUtils.setAuthentication(request, new DefaultAuthentication("", profile));
        }

        processorChain.processRequest(context);
    }

}
