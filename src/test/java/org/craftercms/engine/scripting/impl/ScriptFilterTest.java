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

package org.craftercms.engine.scripting.impl;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.craftercms.commons.config.ConfigUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.plugin.PluginService;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.CacheTemplateMockUtils;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.craftercms.engine.util.groovy.ContentStoreResourceConnector;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ScriptFilter}
 *
 * @author avasquez
 */
public class ScriptFilterTest {

    @Mock
    private CacheTemplate cacheTemplate;
    @Mock
    private ContentStoreService storeService;
    @Mock
    private FilterConfig filterConfig;
    private SiteContext siteContext;
    private ServletContext servletContext;
    private ScriptFilter filter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        CacheTemplateMockUtils.setUpWithNoCaching(cacheTemplate);
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);

        siteContext = createSiteContext(storeService);
        servletContext = new MockServletContext();

        filter = new ScriptFilter();
        filter.setPluginService(mock(PluginService.class));
        filter.setCacheTemplate(cacheTemplate);
        filter.setExcludedUrls(new String[]{
                "/api/1/monitoring/**",
                "/api/1/site/context/**",
                "/api/1/site/cache/**"});

        when(filterConfig.getServletContext()).thenReturn(servletContext);

        filter.init(filterConfig);
    }

    @Test
    public void testFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mymovies");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        setCurrentRequestContext(request, response);
        setCurrentSiteContext(siteContext);

        filter.doFilter(request, response, filterChain);

        String greeting = (String)request.getAttribute("greeting");

        assertNotNull(greeting);
        assertEquals("Hello World!", greeting);
        assertEquals(400, response.getStatus());
        assertEquals("You're not a subscriber", response.getErrorMessage());

        verify(filterChain, never()).doFilter(request, response);

        clearCurrentRequestContext();
    }

    @Test
    public void testFilterExclude() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/static-assets/js/app.js");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        setCurrentRequestContext(request, response);
        setCurrentSiteContext(siteContext);

        filter.doFilter(request, response, filterChain);

        String greeting = (String)request.getAttribute("greeting");

        assertNotNull(greeting);
        assertEquals("Hello World!", greeting);
        assertEquals(200, response.getStatus());

        verify(filterChain).doFilter(request, response);

        clearCurrentRequestContext();
    }

    @Test
    public void testRebuildContextIsIgnored() throws ServletException, IOException {
        ScriptFilter filterSpy = spy(filter);
        String url = "/api/1/site/context/rebuild?token=defaultManagementToken";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", url);
        request.setServletPath(url);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        setCurrentRequestContext(request, response);
        setCurrentSiteContext(siteContext);

        filterSpy.doFilter(request, response, filterChain);

        String greeting = (String)request.getAttribute("greeting");

        assertNull(greeting);

        verify(filterSpy, never()).getScriptFilterChain(any(HttpServletRequest.class), any(FilterChain.class));
    }

    @Test
    public void testNotMatchedRequestIsProcessed() throws ServletException, IOException {
        ScriptFilter filterSpy = spy(filter);
        String url = "/myPage";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", url);
        request.setServletPath(url);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        setCurrentRequestContext(request, response);
        setCurrentSiteContext(siteContext);

        filterSpy.doFilter(request, response, filterChain);

        String greeting = (String)request.getAttribute("greeting");

        assertNotNull(greeting);

        verify(filterSpy, times(1)).getScriptFilterChain(any(HttpServletRequest.class), any(FilterChain.class));
    }

    private SiteContext createSiteContext(ContentStoreService storeService) throws Exception {
        SiteContext siteContext = spy(new SiteContext());
        ScriptFactory scriptFactory = createScriptFactory(siteContext);

        XMLConfiguration config =
            ConfigUtils.readXmlConfiguration(new ClassPathResource("config/site-config.xml"), ',', null);
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));

        when(siteContext.getSiteName()).thenReturn("default");
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getStoreService()).thenReturn(storeService);
        when(siteContext.getConfig()).thenReturn(config);
        when(siteContext.getScriptFactory()).thenReturn(scriptFactory);
        when(siteContext.getCacheTemplate()).thenReturn(cacheTemplate);

        return siteContext;
    }

    private ScriptFactory createScriptFactory(SiteContext siteContext) {
        ContentStoreResourceConnector resourceConnector = new ContentStoreResourceConnector(siteContext);

        return new GroovyScriptFactory(siteContext, resourceConnector, Collections.emptyMap(), false);
    }

    private void setCurrentRequestContext(HttpServletRequest request, HttpServletResponse response) {
        RequestContext.setCurrent(new RequestContext(request, response, null));
    }

    private void setCurrentSiteContext(SiteContext siteContext) {
        SiteContext.setCurrent(siteContext);
    }

    private void clearCurrentRequestContext() {
        RequestContext.clear();
    }

}
