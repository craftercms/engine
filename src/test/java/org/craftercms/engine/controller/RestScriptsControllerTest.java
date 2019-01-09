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

package org.craftercms.engine.controller;

import java.util.Collections;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.controller.rest.RestScriptsController;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.impl.GroovyScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.craftercms.engine.util.groovy.ContentStoreResourceConnector;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    }

    @Test
    public void testHandleRequest() throws Exception {
        MockHttpServletRequest request = createRequest("/test.json");
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);
        setCurrentSiteContext(storeService);

        ModelAndView modelAndView = controller.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        Map<String, Object> responseBody = (Map<String, Object>) modelAndView.getModel().get("responseBody");
        assertEquals("test", responseBody.get("test-param"));
        assertEquals("test", responseBody.get("test-header"));
        assertEquals("test", responseBody.get("test-cookie"));

        removeCurrentRequest();
        removeCurrentSiteContext();
    }

    @Test
    public void testScriptNotFound() throws Exception {
        testError("/testErrorNotFound.json", HttpServletResponse.SC_BAD_REQUEST, "REST script not found");
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

        ModelAndView modelAndView = controller.handleRequest(request, response);

        assertNull(modelAndView);
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

        ModelAndView modelAndView = controller.handleRequest(request, response);

        assertEquals(statusCode, response.getStatus());

        Map<String, Object> responseBody = (Map<String, Object>) modelAndView.getModel().get(
            RestScriptsController.DEFAULT_RESPONSE_BODY_MODEL_ATTR_NAME);
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
        SiteContext siteContext = mock(SiteContext.class);

        ContentStoreResourceConnector resourceConnector = new ContentStoreResourceConnector(siteContext);
        ScriptFactory scriptFactory = new GroovyScriptFactory(resourceConnector,
                                                              Collections.<String, Object>emptyMap());

        when(siteContext.getSiteName()).thenReturn("test");
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getRestScriptsPath()).thenReturn("/scripts");
        when(siteContext.getStoreService()).thenReturn(storeService);
        when(siteContext.getScriptFactory()).thenReturn(scriptFactory);

        return siteContext;
    }

    private void setCurrentSiteContext(ContentStoreService storeService)  {
        SiteContext.setCurrent(createSiteContext(storeService));
    }

    private void removeCurrentSiteContext() {
        SiteContext.clear();
    }

    private MockHttpServletRequest createRequest(String serviceUrl) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://localhost:8080/api/1/services/" +
                                                                           serviceUrl);

        request.setParameter("test-param", "test");
        request.addHeader("test-header", "test");

        Cookie testCookie = new Cookie("test-cookie", "test");
        request.setCookies(testCookie);

        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, serviceUrl);

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
