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

package org.craftercms.engine.util.spring.security.targeting;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bson.types.ObjectId;
import org.craftercms.engine.controller.rest.preview.ProfileRestController;
import org.craftercms.engine.util.spring.security.ConfigAwarePreAuthenticationFilter;
import org.craftercms.profile.api.Profile;

import static org.apache.commons.collections4.MapUtils.isNotEmpty;

/**
 * Implementation of {@link ConfigAwarePreAuthenticationFilter} for Studio targeting
 *
 * @author joseross
 * @since 3.1.5
 */
public class TargetingPreAuthenticatedFilter extends ConfigAwarePreAuthenticationFilter {

    public TargetingPreAuthenticatedFilter() {
        // always enabled
        super(null);
        setAlwaysEnabled(true);
        setSupportedPrincipalClass(TargetingUser.class);

        setCheckForPrincipalChanges(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            Map<String, String> attributes = (Map<String, String>)
                session.getAttribute(ProfileRestController.PROFILE_SESSION_ATTRIBUTE);

            if (isNotEmpty(attributes)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Non-anonymous persona set: " + attributes);
                }

                Profile profile = new Profile();
                profile.setId(new ObjectId(attributes.get("id")));
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

                Map<String, Object> customAttributes = new HashMap<>(attributes);
                customAttributes.remove("id");
                customAttributes.remove("username");
                customAttributes.remove("roles");

                profile.setAttributes(customAttributes);

                return new TargetingUser(new TargetingAuthentication(profile));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("No persona set. Trying to resolve authentication normally");
        }
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
        return "N/A";
    }
    
}
