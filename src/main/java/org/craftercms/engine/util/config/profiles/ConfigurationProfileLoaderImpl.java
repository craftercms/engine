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
package org.craftercms.engine.util.config.profiles;

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationMapper;
import org.craftercms.commons.config.profiles.ConfigurationProfile;
import org.craftercms.commons.config.profiles.ConfigurationProfileLoader;
import org.craftercms.core.service.Content;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;

import java.nio.charset.StandardCharsets;

/**
 * Implementation of {@link ConfigurationProfile} that loads profiles from a specific file in the current site.
 *
 * @author avasquez
 */
public class ConfigurationProfileLoaderImpl<T extends ConfigurationProfile> implements ConfigurationProfileLoader<T> {

    private String profilesUrl;
    private ConfigurationMapper<T> profileMapper;
    private SiteItemService siteItemService;

    @Required
    public void setProfilesUrl(String profilesUrl) {
        this.profilesUrl = profilesUrl;
    }

    @Required
    public void setProfileMapper(ConfigurationMapper<T> profileMapper) {
        this.profileMapper = profileMapper;
    }

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Override
    public T loadProfile(String id) throws ConfigurationException {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            try {
                Content content = siteItemService.getRawContent(profilesUrl);

                return profileMapper.readConfig(content.getInputStream(), StandardCharsets.UTF_8.name(), id);
            } catch (Exception e) {
                throw new ConfigurationException("Error while loading profile " +  id + " from configuration at " +
                                                 profilesUrl, e);
            }
        } else {
            throw new IllegalStateException("No current site context found");
        }
    }

}
