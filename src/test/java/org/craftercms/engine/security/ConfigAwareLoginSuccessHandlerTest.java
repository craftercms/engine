package org.craftercms.engine.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.impl.DefaultAuthentication;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import static org.craftercms.engine.security.ConfigAwareLoginSuccessHandler.LOGIN_DEFAULT_SUCCESS_URL_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.craftercms.engine.security.ConfigAwareLoginSuccessHandler}.
 *
 * @author avasquez
 */
public class ConfigAwareLoginSuccessHandlerTest extends ConfigAwareTestBase {

    private ConfigAwareLoginSuccessHandler handler;
    @Mock
    private RequestCache requestCache;
    @Mock
    private SavedRequest savedRequest;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(savedRequest.getRedirectUrl()).thenReturn("/about");
        when(requestCache.getRequest(any(HttpServletRequest.class), any(HttpServletResponse.class)))
            .thenReturn(savedRequest);

        handler = new ConfigAwareLoginSuccessHandler();
        handler.setDefaultTargetUrl("/");
        handler.setAlwaysUseDefaultTargetUrl(false);
    }

    @Test
    public void testProcessRequest() throws Exception {
        handler.handle(RequestContext.getCurrent(), new DefaultAuthentication(null, new Profile()));

        assertEquals(config.getString(LOGIN_DEFAULT_SUCCESS_URL_KEY),
                     ((MockHttpServletResponse)RequestContext.getCurrent().getResponse()).getRedirectedUrl());
    }
    
}
