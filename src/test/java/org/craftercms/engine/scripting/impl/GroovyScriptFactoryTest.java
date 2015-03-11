package org.craftercms.engine.scripting.impl;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        request.setAttribute(SiteContext.SITE_CONTEXT_ATTRIBUTE, createSiteContext());

        return request;
    }

    private void setCurrentRequest(HttpServletRequest request) {
        RequestContext.setCurrent(new RequestContext(request, null));
    }

    private void removeCurrentRequest() {
        RequestContext.clear();
    }

}
