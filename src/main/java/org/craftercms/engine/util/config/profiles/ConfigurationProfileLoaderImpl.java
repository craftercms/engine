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
package org.craftercms.engine.util.config.profiles;

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationMapper;
import org.craftercms.commons.config.ConfigurationProvider;
import org.craftercms.commons.config.profiles.ConfigurationProfile;
import org.craftercms.commons.config.profiles.ConfigurationProfileLoader;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link ConfigurationProfile} that loads profiles from a specific file in the current site.
 *
 * @author avasquez
 */
public class ConfigurationProfileLoaderImpl<T extends ConfigurationProfile> implements ConfigurationProfileLoader<T> {

    private String profilesModule;
    private String profilesUrl;
    private ConfigurationMapper<T> profileMapper;
    private ConfigurationProvider configurationProvider;

    @Required
    public void setProfilesModule(String profilesModule) {
        this.profilesModule = profilesModule;
    }

    @Required
    public void setProfilesUrl(String profilesUrl) {
        this.profilesUrl = profilesUrl;
    }

    @Required
    public void setProfileMapper(ConfigurationMapper<T> profileMapper) {
        this.profileMapper = profileMapper;
    }

    @Required
    public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public T loadProfile(String id) throws ConfigurationException {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            try {
                return profileMapper.readConfig(configurationProvider, profilesModule, profilesUrl, null, id);
            } catch (Exception e) {
                throw new ConfigurationException("Error while loading profile " +  id + " from configuration at " +
                                                 profilesUrl, e);
            }
        } else {
            throw new IllegalStateException("No current site context found");
        }
    }

}
