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

import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.junit.Before;
import org.junit.Test;

import static org.craftercms.engine.security.ConfigAwareAccessTokenIdResolver.ACCESS_TOKEN_ID_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfigAwareAccessDeniedHandler}.
 *
 * @author avasquez
 */
public class ConfigAwareAccessTokenIdResolverTest extends ConfigAwareTestBase {

    private ConfigAwareAccessTokenIdResolver accessTokenIdResolver;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        accessTokenIdResolver = new ConfigAwareAccessTokenIdResolver();
    }

    @Test
    public void testGetAccessTokenId() throws Exception {
        String accessTokenId = accessTokenIdResolver.getAccessTokenId();

        assertEquals(config.getString(ACCESS_TOKEN_ID_KEY), accessTokenId);
    }

    @Test(expected = ConfigurationException.class)
    public void testGetAccessTokenIdNoConfig() throws Exception {
        when(siteContext.getConfig()).thenReturn(null);

        accessTokenIdResolver.getAccessTokenId();
    }


}
