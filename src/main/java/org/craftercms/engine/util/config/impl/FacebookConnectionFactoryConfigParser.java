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

package org.craftercms.engine.util.config.impl;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.engine.util.config.ConfigurationParser;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;

/**
 * Parses a configuration for the properties of a {@link org.springframework.social.facebook.connect.FacebookConnectionFactory}.
 *
 * @author avasquez
 */
public class FacebookConnectionFactoryConfigParser implements ConfigurationParser<ConnectionFactory<Facebook>> {

    public static final String FACEBOOK_CONNECTION_FACTORY_APP_ID_KEY = "facebookConnectionFactory.appId";
    public static final String FACEBOOK_CONNECTION_FACTORY_APP_SECRET_KEY = "facebookConnectionFactory.appSecret";

    @Override
    public ConnectionFactory<Facebook> parse(HierarchicalConfiguration config) throws ConfigurationException {
        String appId = config.getString(FACEBOOK_CONNECTION_FACTORY_APP_ID_KEY);
        String appSecret = config.getString(FACEBOOK_CONNECTION_FACTORY_APP_SECRET_KEY);

        if (StringUtils.isNotEmpty(appId) && StringUtils.isNotEmpty(appSecret)) {
            return createFacebookConnectionFactory(appId, appSecret);
        } else {
            return null;
        }
    }

    protected ConnectionFactory<Facebook> createFacebookConnectionFactory(String appId, String appSecret) {
        return new FacebookConnectionFactory(appId, appSecret);
    }

}
