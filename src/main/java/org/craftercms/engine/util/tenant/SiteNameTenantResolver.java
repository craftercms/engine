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

package org.craftercms.engine.util.tenant;

import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.security.utils.tenant.TenantsResolver;

/**
 * {@link TenantsResolver} that uses the current site name as tenant.
 *
 * @author avasquez
 */
public class SiteNameTenantResolver implements TenantsResolver {

    @Override
    public String[] getTenants() {
        return new String[] { SiteContext.getCurrent().getSiteName() };
    }

}
