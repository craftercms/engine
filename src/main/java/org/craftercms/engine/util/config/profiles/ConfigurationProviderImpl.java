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

import org.craftercms.commons.config.ConfigurationProvider;
import org.craftercms.engine.service.SiteItemService;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link ConfigurationProvider}
 *
 * @author joseross
 * @since 3.1.6
 */
public class ConfigurationProviderImpl implements ConfigurationProvider {

    protected SiteItemService siteItemService;

    @ConstructorProperties({"siteItemService"})
    public ConfigurationProviderImpl(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Override
    public boolean configExists(String path) {
        return siteItemService.getSiteItem(path) != null;
    }

    @Override
    public InputStream getConfig(String path) throws IOException {
        return siteItemService.getRawContent(path).getInputStream();
    }

}
