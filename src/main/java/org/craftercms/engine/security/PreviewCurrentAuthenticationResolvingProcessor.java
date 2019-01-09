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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.controller.rest.preview.ProfileRestController;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.processors.RequestSecurityProcessorChain;
import org.craftercms.security.processors.impl.CurrentAuthenticationResolvingProcessor;
import org.craftercms.security.utils.SecurityUtils;

/**
 * Obtains and sets the authentication for the current request, using the current Crafter Studio Persona.
 *
 * @author Alfonso Vásquez
 */
public class PreviewCurrentAuthenticationResolvingProcessor extends CurrentAuthenticationResolvingProcessor {

    private static final Log logger = LogFactory.getLog(PreviewCurrentAuthenticationResolvingProcessor.class);

    @Override
    @SuppressWarnings("unchecked")
    public void processRequest(RequestContext context, RequestSecurityProcessorChain processorChain) throws Exception {
        HttpServletRequest request = context.getRequest();
        Map<String, String> attributes = (Map<String, String>)
            request.getSession(true).getAttribute(ProfileRestController.PROFILE_SESSION_ATTRIBUTE);

        if (MapUtils.isNotEmpty(attributes)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Non-anonymous persona set: " + attributes);
            }

            Profile profile = new Profile();
            profile.setUsername("preview");
            profile.setEnabled(true);
            profile.setCreatedOn(new Date());
            profile.setLastModified(new Date());
            profile.setTenant("preview");

            String rolesStr = attributes.get("roles");
            if (rolesStr != null) {
                String[] roles = rolesStr.split(",");
                profile.getRoles().addAll(Arrays.asList(roles));
            }

            Map<String, Object> attributesNoUsernameNoRoles = new HashMap<String, Object>(attributes);
            attributesNoUsernameNoRoles.remove("username");
            attributesNoUsernameNoRoles.remove("roles");

            profile.setAttributes(attributesNoUsernameNoRoles);

            SecurityUtils.setAuthentication(request, new PersonaAuthentication(profile));

            processorChain.processRequest(context);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No persona set. Trying to resolve authentication normally");
            }

            super.processRequest(context, processorChain);
        }
    }

}
