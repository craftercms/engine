package org.craftercms.engine.scripting.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import groovy.lang.GroovyClassLoader;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.craftercms.engine.util.groovy.ContentStoreGroovyResourceLoader;
import org.craftercms.engine.util.groovy.ContentStoreResourceConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alfonso Vásquez
 */
public class GroovyScriptFactoryTest {

    private ContentStoreService storeService;
    private ScriptFactory scriptFactory;
    private GroovyClassLoader classLoader;
    private Map<String, Object> globalVars;

    @Before
    public void setUp() throws Exception {
        storeService = createContentStoreService();

        setCurrentRequest(createRequest());
        setCurrentSiteContext(createSiteContext(storeService));

        classLoader = createGroovyClassLoader();
        globalVars = createGlobalVars(classLoader);
        scriptFactory = createScriptFactory(classLoader, globalVars);
    }

    @After
    public void tearDown() throws Exception {
        removeCurrentRequest();
        destroyApplicationContext();
    }

    @Test
    public void testExecuteScript() throws Exception {
        Map<String, Object> vars = Collections.<String, Object>singletonMap("name", "Alfonso");

        String result = (String) scriptFactory.getScript("/scripts/testImport.get.groovy").execute(vars);

        assertEquals("Hello Alfonso!", result);

        result = (String) scriptFactory.getScript("/scripts/testAppContext.get.groovy").execute(vars);

        assertEquals("Hello Alfonso!", result);
    }

    private SiteContext createSiteContext(ContentStoreService storeService) {
        SiteContext siteContext = mock(SiteContext.class);
        when(siteContext.getSiteName()).thenReturn("default");
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getStoreService()).thenReturn(storeService);

        return siteContext;
    }

    private ContentStoreService createContentStoreService() {
        ContentStoreService storeService = mock(ContentStoreService.class);
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);

        return storeService;
    }

    private GroovyClassLoader createGroovyClassLoader() {
        ContentStoreGroovyResourceLoader resourceLoader = new ContentStoreGroovyResourceLoader(SiteContext.getCurrent(),
                                                                                               "/classes");
        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().getClassLoader());

        classLoader.setResourceLoader(resourceLoader);

        return classLoader;
    }

    private Map<String, Object> createGlobalVars(GroovyClassLoader classLoader) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setClassLoader(classLoader);

        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(context);
        xmlReader.loadBeanDefinitions(new ClassPathResource("config/spring/application-context.xml"));

        context.refresh();

        Map<String, Object> globalVars = new HashMap<>(1);
        globalVars.put("applicationContext", context);

        return globalVars;
    }

    private ScriptFactory createScriptFactory(GroovyClassLoader parentClassLoader, Map<String, Object> globalVars) {
        ContentStoreResourceConnector resourceConnector = new ContentStoreResourceConnector(SiteContext.getCurrent());

        return new GroovyScriptFactory(resourceConnector, parentClassLoader, globalVars);
    }

    private void setCurrentSiteContext(SiteContext siteContext) {
        SiteContext.setCurrent(siteContext);
    }

    private void removeCurrentSiteContext() {
        SiteContext.clear();
    }

    private MockHttpServletRequest createRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        return request;
    }

    private void setCurrentRequest(HttpServletRequest request) {
        RequestContext.setCurrent(new RequestContext(request, null, null));
    }

    private void removeCurrentRequest() {
        RequestContext.clear();
    }

    private void destroyApplicationContext() {
        ((GenericApplicationContext)globalVars.get("applicationContext")).destroy();
    }

}
