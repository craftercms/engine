package org.craftercms.engine.security;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.impl.DefaultAuthentication;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.craftercms.engine.security.ConfigAwareLogoutSuccessHandler.LOGOUT_SUCCESS_URL_KEY;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link org.craftercms.engine.security.ConfigAwareLogoutSuccessHandler}.
 *
 * @author avasquez
 */
public class ConfigAwareLogoutSuccessHandlerTest extends ConfigAwareTestBase {

    private ConfigAwareLogoutSuccessHandler handler;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        handler = new ConfigAwareLogoutSuccessHandler();
        handler.setTargetUrl("/");
    }

    @Test
    public void testProcessRequest() throws Exception {
        handler.handle(RequestContext.getCurrent(), new DefaultAuthentication(null, new Profile()));

        assertEquals(config.getString(LOGOUT_SUCCESS_URL_KEY),
                     ((MockHttpServletResponse)RequestContext.getCurrent().getResponse()).getRedirectedUrl());
    }
    
}
