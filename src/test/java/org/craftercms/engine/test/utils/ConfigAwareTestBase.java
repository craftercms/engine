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

package org.craftercms.engine.test.utils;

import org.apache.commons.configuration2.XMLConfiguration;
import org.craftercms.commons.config.ConfigUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.service.Context;
import org.craftercms.engine.service.context.SiteContext;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.when;

/**
 * Base for security config aware related tests.
 *
 * @author avasquez
 */
public class ConfigAwareTestBase {

    @Spy
    protected SiteContext siteContext;
    protected XMLConfiguration config;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(siteContext.getSiteName()).thenReturn("test");
        when(siteContext.getContext()).thenReturn(Mockito.mock(Context.class));

        setCurrentRequestContext();
        setCurrentSiteContext();

        config = ConfigUtils.readXmlConfiguration(new ClassPathResource("config/site-config.xml"), ',', null);

        when(siteContext.getConfig()).thenReturn(config);
    }

    @After
    public void tearDown() throws Exception {
        clearCurrentRequestContext();
    }

    private void setCurrentSiteContext() {
        SiteContext.setCurrent(siteContext);
    }

    private void setCurrentRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setPathInfo("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContext context = new RequestContext(request, response, null);

        RequestContext.setCurrent(context);
    }

    private void clearCurrentRequestContext() {
        RequestContext.clear();
    }

}
