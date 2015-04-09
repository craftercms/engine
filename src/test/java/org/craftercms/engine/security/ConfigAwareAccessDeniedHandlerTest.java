package org.craftercms.engine.security;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.craftercms.security.exception.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.craftercms.engine.security.ConfigAwareAccessDeniedHandler.ACCESS_DENIED_ERROR_PAGE_URL_KEY;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link org.craftercms.engine.security.ConfigAwareAccessDeniedHandler}.
 *
 * @author avasquez
 */
public class ConfigAwareAccessDeniedHandlerTest extends ConfigAwareTestBase {

    private ConfigAwareAccessDeniedHandler handler;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        handler = new ConfigAwareAccessDeniedHandler();
        handler.setErrorPageUrl("/access-denied");
    }

    @Test
    public void testProcessRequest() throws Exception {
        handler.handle(RequestContext.getCurrent(), new AccessDeniedException(""));

        assertEquals(config.getString(ACCESS_DENIED_ERROR_PAGE_URL_KEY),
                     ((MockHttpServletResponse)RequestContext.getCurrent().getResponse()).getForwardedUrl());
    }
    
}
