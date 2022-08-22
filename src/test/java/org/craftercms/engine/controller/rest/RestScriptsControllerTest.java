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

package org.craftercms.engine.controller.rest;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.plugin.PluginService;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.impl.GroovyScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.CacheTemplateMockUtils;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.craftercms.engine.util.groovy.ContentStoreResourceConnector;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link org.craftercms.engine.controller.rest.RestScriptsController}
 *
 * @author Alfonso Vásquez
 */
public class RestScriptsControllerTest {

    private ContentStoreService storeService;
    private RestScriptsController controller;

    @Before
    public void setUp() throws Exception {
        storeService = createContentStoreService();

        controller = new RestScriptsController();
        controller.setServletContext(createServletContext());

        PluginService pluginService = mock(PluginService.class);
        controller.setPluginService(pluginService);
    }

    @Test
    public void testHandleRequest() throws Exception {
        MockHttpServletRequest request = createRequest("/test.json");
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);
        setCurrentSiteContext(storeService);

        ResponseEntity<Map<String,Object>> responseEntity = controller.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        Map<String, Object> responseBody = responseEntity.getBody();
        assertEquals("test", responseBody.get("test-param"));
        assertEquals("test", responseBody.get("test-header"));
        assertEquals("test", responseBody.get("test-cookie"));

        removeCurrentRequest();
        removeCurrentSiteContext();
    }

    @Test
    public void testScriptNotFound() throws Exception {
        testError("/testErrorNotFound.json", HttpServletResponse.SC_NOT_FOUND, "REST script not found");
    }

    @Test
    public void testError() throws Exception {
        testError("/testError.json", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "This is an error test message");
    }

    @Test
    public void testError2() throws Exception {
        testError("/testError2.json", HttpServletResponse.SC_BAD_REQUEST, "This is an error test message");
    }

    @Test
    public void testRedirect() throws Exception {
        MockHttpServletRequest request = createRequest("/testRedirect.json");
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);
        setCurrentSiteContext(storeService);

        ResponseEntity responseEntity = controller.handleRequest(request, response);

        assertNull(responseEntity.getBody());
        assertTrue(response.isCommitted());
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
        assertEquals("/api/1/services/test.json", response.getRedirectedUrl());

        removeCurrentRequest();
        removeCurrentSiteContext();
    }

    @SuppressWarnings("unchecked")
    private void testError(String serviceUrl, int statusCode, String message) throws Exception {
        MockHttpServletRequest request = createRequest(serviceUrl);
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);
        setCurrentSiteContext(storeService);

        ResponseEntity responseEntity = controller.handleRequest(request, response);

        assertEquals(statusCode, response.getStatus());

        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertEquals(message, responseBody.get(RestScriptsController.DEFAULT_ERROR_MESSAGE_MODEL_ATTR_NAME));

        removeCurrentRequest();
        removeCurrentSiteContext();
    }

    private ServletContext createServletContext() {
        return mock(ServletContext.class);
    }

    private ContentStoreService createContentStoreService() {
        ContentStoreService storeService = mock(ContentStoreService.class);
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);

        return storeService;
    }

    private SiteContext createSiteContext(ContentStoreService storeService) {
        SiteContext siteContext = spy(new SiteContext());
        CacheTemplate cacheTemplate = CacheTemplateMockUtils.createCacheTemplate();

        ContentStoreResourceConnector resourceConnector = new ContentStoreResourceConnector(siteContext);
        ScriptFactory scriptFactory =
                new GroovyScriptFactory(siteContext, resourceConnector, Collections.emptyMap(), false);

        when(siteContext.getSiteName()).thenReturn("test");
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getRestScriptsPath()).thenReturn("/scripts");
        when(siteContext.getStoreService()).thenReturn(storeService);
        when(siteContext.getScriptFactory()).thenReturn(scriptFactory);
        when(siteContext.getCacheTemplate()).thenReturn(cacheTemplate);

        return siteContext;
    }

    private void setCurrentSiteContext(ContentStoreService storeService)  {
        SiteContext.setCurrent(createSiteContext(storeService));
    }

    private void removeCurrentSiteContext() {
        SiteContext.clear();
    }

    private MockHttpServletRequest createRequest(String serviceUrl) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", serviceUrl);

        request.setParameter("test-param", "test");
        request.addHeader("test-header", "test");

        Cookie testCookie = new Cookie("test-cookie", "test");
        request.setCookies(testCookie);

        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, serviceUrl);
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/**");

        return request;
    }

    private MockHttpServletResponse createResponse() {
        return new MockHttpServletResponse();
    }

    private void setCurrentRequest(HttpServletRequest request) {
        RequestContext.setCurrent(new RequestContext(request, null, null));
    }

    private void removeCurrentRequest() {
        RequestContext.clear();
    }

}
