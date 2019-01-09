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

package org.craftercms.engine.util.spring.social;

import java.util.Arrays;

import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.util.config.ConfigurationParser;
import org.craftercms.engine.util.config.impl.FacebookConnectionFactoryConfigParser;
import org.craftercms.engine.test.utils.CacheTemplateMockUtils;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookAdapter;
import org.springframework.social.facebook.connect.FacebookServiceProvider;

import static org.craftercms.engine.util.config.impl.FacebookConnectionFactoryConfigParser.*;
import static org.craftercms.engine.util.spring.social.ConfigAwareConnectionFactoryLocator.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link org.craftercms.engine.util.spring.social.ConfigAwareConnectionFactoryLocator}.
 *
 * @author avasquez
 */
public class ConfigAwareConnectionFactoryLocatorTest extends ConfigAwareTestBase {

    private ConfigAwareConnectionFactoryLocator locator;
    @Mock
    private CacheTemplate cacheTemplate;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        CacheTemplateMockUtils.setUpWithNoCaching(cacheTemplate);

        ConfigurationParser<?> configParserStub = new FacebookConnectionFactoryConfigParser() {

            @Override
            protected ConnectionFactory<Facebook> createFacebookConnectionFactory(String appId, String appSecret) {
                return new FacebookConnectionFactoryStub(appId, appSecret);
            }

        };

        locator = new ConfigAwareConnectionFactoryLocator();
        locator.setCacheTemplate(cacheTemplate);
        locator.setDefaultLocator(new ConnectionFactoryRegistry());
        locator.setConfigParsers(Arrays.<ConfigurationParser<?>>asList(configParserStub));
    }

    @Test
    public void testGetConnectionFactoryWithProviderId() throws Exception {
        FacebookConnectionFactoryStub factory = (FacebookConnectionFactoryStub)locator.getConnectionFactory("facebook");

        assertNotNull(factory);
        assertEquals(config.getString(SOCIAL_CONNECTIONS_KEY + "." + FACEBOOK_CONNECTION_FACTORY_APP_ID_KEY), factory.appId);
        assertEquals(config.getString(SOCIAL_CONNECTIONS_KEY + "." + FACEBOOK_CONNECTION_FACTORY_APP_SECRET_KEY), factory.appSecret);
    }

    @Test
    public void testGetConnectionFactoryWithApiType() throws Exception {
        FacebookConnectionFactoryStub factory = (FacebookConnectionFactoryStub)locator.getConnectionFactory(Facebook.class);

        assertNotNull(factory);
        assertEquals(config.getString(SOCIAL_CONNECTIONS_KEY + "." + FACEBOOK_CONNECTION_FACTORY_APP_ID_KEY), factory.appId);
        assertEquals(config.getString(SOCIAL_CONNECTIONS_KEY + "." + FACEBOOK_CONNECTION_FACTORY_APP_SECRET_KEY), factory.appSecret);
    }

    private static class FacebookConnectionFactoryStub extends ConnectionFactory<Facebook> {

        private String appId;
        private String appSecret;

        public FacebookConnectionFactoryStub(String appId, String appSecret) {
            super("facebook", new FacebookServiceProvider(appId, appSecret, null), new FacebookAdapter());

            this.appId = appId;
            this.appSecret = appSecret;
        }

        @Override
        public Connection<Facebook> createConnection(ConnectionData data) {
            return null;
        }
    }

}
