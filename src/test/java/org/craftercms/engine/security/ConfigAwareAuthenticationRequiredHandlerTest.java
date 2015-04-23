package org.craftercms.engine.security;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.craftercms.security.exception.AuthenticationException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.craftercms.engine.security.ConfigAwareAuthenticationRequiredHandler.LOGIN_FORM_URL_KEY;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link org.craftercms.engine.security.ConfigAwareAuthenticationRequiredHandler}.
 *
 * @author avasquez
 */
public class ConfigAwareAuthenticationRequiredHandlerTest extends ConfigAwareTestBase {

    private ConfigAwareAuthenticationRequiredHandler handler;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        handler = new ConfigAwareAuthenticationRequiredHandler();
        handler.setLoginFormUrl("/login");
    }

    @Test
    public void testProcessRequest() throws Exception {
        handler.handle(RequestContext.getCurrent(), new AuthenticationException());

        assertEquals(config.getString(LOGIN_FORM_URL_KEY),
                     ((MockHttpServletResponse)RequestContext.getCurrent().getResponse()).getRedirectedUrl());
    }
    
}
