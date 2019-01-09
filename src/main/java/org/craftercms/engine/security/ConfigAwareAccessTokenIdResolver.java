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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.profile.services.impl.AccessTokenIdResolver;

/**
 * Special {@link AccessTokenIdResolver} used in multi-tenant engine that returns the access token ID from the site
 * configuration, throwing an {@link ConfigurationException} if not found.
 *
 * @author avasquez
 */
public class ConfigAwareAccessTokenIdResolver implements AccessTokenIdResolver {

    public static final String ACCESS_TOKEN_ID_KEY = "profile.api.accessTokenId";

    @Override
    public String getAccessTokenId() {
        String accessTokenId = null;
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();

        if (config != null) {
            accessTokenId = config.getString(ACCESS_TOKEN_ID_KEY);
        }

        if (StringUtils.isNotEmpty(accessTokenId)) {
            return accessTokenId;
        } else {
            throw new ConfigurationException("Current config for site '" + SiteContext.getCurrent().getSiteName() +
                                            "' doesn't contain required property " + ACCESS_TOKEN_ID_KEY);
        }
    }

}
