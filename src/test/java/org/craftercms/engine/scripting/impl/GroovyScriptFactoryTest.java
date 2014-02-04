package org.craftercms.engine.scripting.impl;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Alfonso Vásquez
 */
public class GroovyScriptFactoryTest {

    private ContentStoreService storeService;
    private ScriptFactory scriptFactory;

    @Before
    public void setUp() throws Exception {
        storeService = createContentStoreService();
        scriptFactory = createScriptFactory(storeService);

        setCurrentRequest(createRequest());
    }

    @After
    public void tearDown() throws Exception {
        removeCurrentRequest();
    }

    @Test
    public void testExecuteScript() throws Exception {
        Script script = scriptFactory.getScript("/scripts/rest/testImport.get.groovy");
        Map vars = Collections.singletonMap("name", "Alfonso");

        String result = (String) script.execute(vars);

        assertEquals("Hello Alfonso!", result);

        verify(storeService, atLeastOnce()).getContent(any(Context.class), anyString());
        verify(storeService, atLeastOnce()).getContent(any(Context.class), eq("/scripts/shared/Greeting.groovy"));
    }

    private SiteContext createSiteContext() throws Exception {
        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getContext()).thenReturn(mock(Context.class));

        return siteContext;
    }

    private ContentStoreService createContentStoreService() {
        ContentStoreService storeService = mock(ContentStoreService.class);
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);

        return storeService;
    }

    private ScriptFactory createScriptFactory(ContentStoreService storeService) {
        ContentResourceConnector resourceConnector = new ContentResourceConnector();
        resourceConnector.setStoreService(storeService);

        GroovyScriptFactory scriptFactory = new GroovyScriptFactory();
        scriptFactory.setResourceConnector(resourceConnector);

        return scriptFactory;
    }

    private MockHttpServletRequest createRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AbstractSiteContextResolvingFilter.SITE_CONTEXT_ATTRIBUTE, createSiteContext());

        return request;
    }

    private void setCurrentRequest(HttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void removeCurrentRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

}
