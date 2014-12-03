package org.craftercms.engine.controller;

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.controller.rest.RestScriptsController;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.impl.Jsr233CompiledScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.craftercms.engine.test.utils.CacheTemplateMockUtils;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
        CacheTemplate cacheTemplate = createCacheTemplate();

        storeService = createContentStoreService();

        controller = new RestScriptsController();
        controller.setScriptFactory(createScriptFactory(cacheTemplate, storeService));
        controller.setServletContext(createServletContext());
    }

    @Test
    public void testHandleRequest() throws Exception {
        MockHttpServletRequest request = createRequest("/test.json");
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);

        ModelAndView modelAndView = controller.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        Map<String, Object> responseBody = (Map<String, Object>) modelAndView.getModel().get("responseBody");
        assertEquals("test", responseBody.get("test-param"));
        assertEquals("test", responseBody.get("test-header"));
        assertEquals("test", responseBody.get("test-cookie"));

        removeCurrentRequest();
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

        ModelAndView modelAndView = controller.handleRequest(request, response);

        assertNull(modelAndView);
        assertTrue(response.isCommitted());
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
        assertEquals("/api/1/services/test.json", response.getRedirectedUrl());

        removeCurrentRequest();
    }

    private void testError(String serviceUrl, int statusCode, String message) throws Exception {
        MockHttpServletRequest request = createRequest(serviceUrl);
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);

        ModelAndView modelAndView = controller.handleRequest(request, response);

        assertEquals(statusCode, response.getStatus());

        Map<String, Object> responseBody = (Map<String, Object>) modelAndView.getModel().get(RestScriptsController
            .DEFAULT_RESPONSE_BODY_MODEL_ATTR_NAME);
        assertEquals(message, responseBody.get(RestScriptsController.DEFAULT_ERROR_MESSAGE_MODEL_ATTR_NAME));

        removeCurrentRequest();
    }

    private ServletContext createServletContext() {
        return mock(ServletContext.class);
    }

    private ContentStoreService createContentStoreService() {
        ContentStoreService storeService = mock(ContentStoreService.class);
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);

        return storeService;
    }

    private CacheTemplate createCacheTemplate() {
        CacheTemplate cacheTemplate = mock(CacheTemplate.class);
        CacheTemplateMockUtils.setUpExecuteWithNoCaching(cacheTemplate);

        return cacheTemplate;
    }

    private SiteContext createSiteContext() throws Exception {
        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getRestScriptsPath()).thenReturn("/scripts/rest");

        return siteContext;
    }

    private ScriptFactory createScriptFactory(CacheTemplate cacheTemplate, ContentStoreService storeService) {
        Jsr233CompiledScriptFactory scriptFactory = new Jsr233CompiledScriptFactory();
        scriptFactory.setCacheTemplate(cacheTemplate);
        scriptFactory.setStoreService(storeService);
        scriptFactory.init();

        return scriptFactory;
    }

    private MockHttpServletRequest createRequest(String serviceUrl) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://localhost:8080/api/1/services/" +
            serviceUrl);

        request.setParameter("test-param", "test");
        request.addHeader("test-header", "test");

        Cookie testCookie = new Cookie("test-cookie", "test");
        request.setCookies(testCookie);

        request.setAttribute(AbstractSiteContextResolvingFilter.SITE_CONTEXT_ATTRIBUTE, createSiteContext());
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, serviceUrl);

        return request;
    }

    private MockHttpServletResponse createResponse() {
        return new MockHttpServletResponse();
    }

    private void setCurrentRequest(HttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void removeCurrentRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

}
