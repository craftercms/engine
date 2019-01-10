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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.impl.DefaultAuthentication;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import static org.craftercms.engine.security.ConfigAwareLoginSuccessHandler.LOGIN_DEFAULT_SUCCESS_URL_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.craftercms.engine.security.ConfigAwareLoginSuccessHandler}.
 *
 * @author avasquez
 */
public class ConfigAwareLoginSuccessHandlerTest extends ConfigAwareTestBase {

    private ConfigAwareLoginSuccessHandler handler;
    @Mock
    private RequestCache requestCache;
    @Mock
    private SavedRequest savedRequest;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(savedRequest.getRedirectUrl()).thenReturn("/about");
        when(requestCache.getRequest(any(HttpServletRequest.class), any(HttpServletResponse.class)))
            .thenReturn(savedRequest);

        handler = new ConfigAwareLoginSuccessHandler();
        handler.setDefaultTargetUrl("/");
        handler.setAlwaysUseDefaultTargetUrl(false);
    }

    @Test
    public void testProcessRequest() throws Exception {
        handler.handle(RequestContext.getCurrent(), new DefaultAuthentication(null, new Profile()));

        assertEquals(config.getString(LOGIN_DEFAULT_SUCCESS_URL_KEY),
                     ((MockHttpServletResponse)RequestContext.getCurrent().getResponse()).getRedirectedUrl());
    }
    
}
