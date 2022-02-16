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

package org.craftercms.engine.util.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ConfigAwareLogoutSuccessHandler}.
 *
 * @author avasquez
 */
public class ConfigAwareLogoutSuccessHandlerTest extends ConfigAwareTestBase {

    private ConfigAwareLogoutSuccessHandler handler;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        handler = new ConfigAwareLogoutSuccessHandler();
        handler.setDefaultTargetUrl("/");
    }

    @Test
    public void testProcessRequest() throws Exception {
        HttpServletRequest request = RequestContext.getCurrent().getRequest();
        HttpServletResponse response = RequestContext.getCurrent().getResponse();
        handler.onLogoutSuccess(request, response, new UsernamePasswordAuthenticationToken("", ""));

        assertEquals(config.getString(ConfigAwareLogoutSuccessHandler.LOGOUT_SUCCESS_URL_KEY),
                     ((MockHttpServletResponse)RequestContext.getCurrent().getResponse()).getRedirectedUrl());
    }
    
}
