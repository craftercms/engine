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
package org.craftercms.engine.security;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.security.annotations.RunIfSecurityEnabled;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * Manages access to Crafter pages, depending on the roles specified in the page and the current user's roles.
 *
 * @author Russ Danner
 * @author Alfonso Vásquez
 */
public class CrafterPageAccessManager {

    private static final String ROLE_PREFIX = "ROLE_";

    protected String authorizedRolesXPathQuery;

    @Required
    public void setAuthorizedRolesXPathQuery(String authorizedRolesXPathQuery) {
        this.authorizedRolesXPathQuery = authorizedRolesXPathQuery;
    }

    /**
     * Checks if the user has sufficient rights to access the specified page:
     *
     * <ol>
     *     <li>If the page doesn't contain any required role, no authentication is needed.</li>
     *     <li>If the page has the role "Anonymous", no authentication is needed.</li>
     *     <li>If the page has the role "Authenticated", just authentication is needed.</li>
     *     <li>If the page has any other the roles, the user needs to have any of those roles.</li>
     * </ol>
     */
    @RunIfSecurityEnabled
    public void checkAccess(SiteItem page) {
        String pageUrl = page.getStoreUrl();
        Authentication auth = null;

        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            auth = context.getAuthentication();
        }

        List<String> authorizedRoles = getAuthorizedRolesForPage(page);

        if (CollectionUtils.isNotEmpty(authorizedRoles) && !containsRole("anonymous", authorizedRoles)) {
            // If auth == null it is anonymous
            if (auth == null || auth instanceof AnonymousAuthenticationToken) {
                throw new AccessDeniedException("User is anonymous but page '" + pageUrl + "' requires authentication");
            }
            if (!containsRole("authenticated", authorizedRoles) && !hasAnyRole(auth, authorizedRoles)) {
                throw new AccessDeniedException("User '" + auth.getName() + "' is not authorized " +
                                                "to view page '" + pageUrl + "'");
            }
        }
    }

    protected List<String> getAuthorizedRolesForPage(SiteItem page) {
        return page.queryValues(authorizedRolesXPathQuery);
    }

    protected boolean containsRole(String role, List<String> roles) {
        for (String r : roles) {
            if (removeStart(r, ROLE_PREFIX).equalsIgnoreCase(role)) {
                return true;
            }
        }

        return false;
    }

    protected boolean hasAnyRole(Authentication auth, List<String> roles) {
        return auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(authority -> removeStart(authority, ROLE_PREFIX))
            .anyMatch(authority -> containsRole(authority, roles));
    }

}
