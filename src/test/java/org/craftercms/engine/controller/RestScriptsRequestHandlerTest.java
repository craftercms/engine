package org.craftercms.engine.controller;

import com.google.gson.Gson;
import freemarker.cache.TemplateLoader;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.freemarker.CrafterFreeMarkerTemplateLoader;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.scripting.ScriptViewResolver;
import org.craftercms.engine.scripting.impl.FreeMarkerScriptViewResolver;
import org.craftercms.engine.scripting.impl.Jsr233CompiledScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.craftercms.engine.test.utils.CacheTemplateMockUtils;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringReader;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test for {@link RestScriptsRequestHandler}
 *
 * @author Alfonso Vásquez
 */
public class RestScriptsRequestHandlerTest {

    private ContentStoreService storeService;
    private RestScriptsRequestHandler handler;

    @Before
    public void setUp() throws Exception {
        CacheTemplate cacheTemplate = createCacheTemplate();
        ServletContext servletContext = createServletContext();

        storeService = createContentStoreService();

        handler = new RestScriptsRequestHandler();
        handler.setScriptFactory(createScriptFactory(cacheTemplate, storeService));
        handler.setScriptViewResolver(createScriptViewResolver(cacheTemplate, servletContext));
        handler.setServletContext(servletContext);
    }

    @Test
    public void testHandleJsonRequest() throws Exception {
        MockHttpServletRequest request = createRequest("/test.json", storeService);
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);

        handler.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Gson gson = new Gson();
        Map<String, Object> result = gson.fromJson(response.getContentAsString(), Map.class);

        assertEquals("test", result.get("test-param"));
        assertEquals("test", result.get("test-header"));
        assertEquals("test", result.get("test-cookie"));
        assertEquals("test", result.get("test-attribute"));

        removeCurrentRequest();
    }

    @Test
    public void testHandleJsonRequest2() throws Exception {
        MockHttpServletRequest request = createRequest("/test", storeService);
        MockHttpServletResponse response = createResponse();

        request.setParameter("format", "json");

        setCurrentRequest(request);

        handler.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Gson gson = new Gson();
        Map<String, Object> result = gson.fromJson(response.getContentAsString(), Map.class);

        assertEquals("json", result.get("format"));
        assertEquals("test", result.get("test-param"));
        assertEquals("test", result.get("test-header"));
        assertEquals("test", result.get("test-cookie"));
        assertEquals("test", result.get("test-attribute"));

        removeCurrentRequest();
    }

    @Test
    public void testHandleJsonRequest3() throws Exception {
        MockHttpServletRequest request = createRequest("/test", storeService);
        MockHttpServletResponse response = createResponse();

        request.addHeader("Accept", "application/json");

        setCurrentRequest(request);

        handler.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Gson gson = new Gson();
        Map<String, Object> result = gson.fromJson(response.getContentAsString(), Map.class);

        assertEquals("application/json", result.get("Accept"));
        assertEquals("test", result.get("test-param"));
        assertEquals("test", result.get("test-header"));
        assertEquals("test", result.get("test-cookie"));
        assertEquals("test", result.get("test-attribute"));

        removeCurrentRequest();
    }

    @Test
    public void testHandleXmlRequest() throws Exception {
        MockHttpServletRequest request = createRequest("/test.xml", storeService);
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);

        handler.handleRequest(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/xml", response.getContentType());

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(response.getContentAsString()));

        assertEquals("test", XmlUtils.selectSingleNodeValue(document, "//value[@key=\"test-param\"]"));
        assertEquals("test", XmlUtils.selectSingleNodeValue(document, "//value[@key=\"test-header\"]"));
        assertEquals("test", XmlUtils.selectSingleNodeValue(document, "//value[@key=\"test-cookie\"]"));
        assertEquals("test", XmlUtils.selectSingleNodeValue(document, "//value[@key=\"test-attribute\"]"));

        removeCurrentRequest();
    }

    @Test
    public void testError1() throws Exception {
        testError("/testError.json", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testError2() throws Exception {
        testError("/testError2.json", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testError3() throws Exception {
        testError("/testError3.json", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testError4() throws Exception {
        testError("/testError4.json", HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testError5() throws Exception {
        testError("/testError5.json", HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testError6() throws Exception {
        testError("/testError6.json", HttpServletResponse.SC_FORBIDDEN);
    }

    private void testError(String serviceUrl, int statusCode) throws Exception {
        MockHttpServletRequest request = createRequest(serviceUrl, storeService);
        MockHttpServletResponse response = createResponse();

        setCurrentRequest(request);

        handler.handleRequest(request, response);

        Gson gson = new Gson();
        Map<String, Object> result = gson.fromJson(response.getContentAsString(), Map.class);

        assertEquals(statusCode, response.getStatus());
        assertEquals("application/json", response.getContentType());

        assertEquals((double) statusCode, result.get("status"));
        assertEquals("This is an error test message", result.get("message"));

        removeCurrentRequest();
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

    private ServletContext createServletContext() {
        return mock(ServletContext.class);
    }

    private SiteContext createSiteContext(ContentStoreService storeService) throws Exception {
        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getRestScriptsPath()).thenReturn("/scripts");
        when(siteContext.getRestScriptTemplatesPath()).thenReturn("/scripts");
        when(siteContext.getRestScriptsFreeMarkerConfig()).thenReturn(createScriptsFreeMarkerConfig(storeService));

        return siteContext;
    }

    private FreeMarkerConfig createScriptsFreeMarkerConfig(ContentStoreService storeService) throws Exception {
        CrafterFreeMarkerTemplateLoader templateLoader = new CrafterFreeMarkerTemplateLoader();
        templateLoader.setContentStoreService(storeService);
        templateLoader.setUseRestScriptTemplatesPath(true);

        Properties freeMarkerProperties = new Properties();
        freeMarkerProperties.setProperty("output_encoding", "UTF-8");
        freeMarkerProperties.setProperty("object_wrapper", "org.craftercms.engine.freemarker.CrafterObjectWrapper");

        FreeMarkerConfigurer config = new FreeMarkerConfigurer();
        config.setPreTemplateLoaders(new TemplateLoader[] {templateLoader});
        config.setDefaultEncoding("UTF-8");
        config.setFreemarkerSettings(freeMarkerProperties);
        config.afterPropertiesSet();

        return config;
    }

    private ScriptFactory createScriptFactory(CacheTemplate cacheTemplate, ContentStoreService storeService) {
        Jsr233CompiledScriptFactory scriptFactory = new Jsr233CompiledScriptFactory();
        scriptFactory.setCacheTemplate(cacheTemplate);
        scriptFactory.setStoreService(storeService);
        scriptFactory.init();

        return scriptFactory;
    }

    private ScriptViewResolver createScriptViewResolver(CacheTemplate cacheTemplate, ServletContext servletContext) {
        FreeMarkerScriptViewResolver viewResolver = new FreeMarkerScriptViewResolver();
        viewResolver.setCacheTemplate(cacheTemplate);
        viewResolver.setServletContext(servletContext);

        return viewResolver;
    }

    private LocaleResolver createLocaleResolver() {
        LocaleResolver localeResolver = mock(LocaleResolver.class);
        when(localeResolver.resolveLocale(any(HttpServletRequest.class))).thenReturn(Locale.US);

        return localeResolver;
    }

    private MockHttpServletRequest createRequest(String serviceUrl, ContentStoreService storeService) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://localhost:8080/api/1/services/" + serviceUrl);

        request.setParameter("test-param", "test");
        request.addHeader("test-header", "test");

        Cookie testCookie = new Cookie("test-cookie", "test");
        request.setCookies(testCookie);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("test-attribute", "test");
        request.setSession(session);

        request.setAttribute(AbstractSiteContextResolvingFilter.SITE_CONTEXT_ATTRIBUTE, createSiteContext(storeService));
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, serviceUrl);
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, createLocaleResolver());

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
