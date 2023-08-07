/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.util.SecurityUtils;
import org.craftercms.security.annotations.RunIfSecurityEnabled;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * Manages access to Crafter pages, depending on the roles specified in the page and the current user's roles.
 *
 * @author Russ Danner
 * @author Alfonso Vásquez
 */
public class CrafterPageAccessManager {

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

        List<String> authorizedRoles = getAuthorizedRolesForPage(page);
        SecurityUtils.checkAccess(authorizedRoles, pageUrl);
    }

    protected List<String> getAuthorizedRolesForPage(SiteItem page) {
        return page.queryValues(authorizedRolesXPathQuery);
    }

}
