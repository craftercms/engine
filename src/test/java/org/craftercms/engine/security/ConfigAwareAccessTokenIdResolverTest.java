package org.craftercms.engine.security;

import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.junit.Before;
import org.junit.Test;

import static org.craftercms.engine.security.ConfigAwareAccessTokenIdResolver.ACCESS_TOKEN_ID_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfigAwareAccessDeniedHandler}.
 *
 * @author avasquez
 */
public class ConfigAwareAccessTokenIdResolverTest extends ConfigAwareTestBase {

    private ConfigAwareAccessTokenIdResolver accessTokenIdResolver;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        accessTokenIdResolver = new ConfigAwareAccessTokenIdResolver();
    }

    @Test
    public void testGetAccessTokenId() throws Exception {
        String accessTokenId = accessTokenIdResolver.getAccessTokenId();

        assertEquals(config.getString(ACCESS_TOKEN_ID_KEY), accessTokenId);
    }

    @Test(expected = ConfigurationException.class)
    public void testGetAccessTokenIdNoConfig() throws Exception {
        when(context.getConfig()).thenReturn(null);

        accessTokenIdResolver.getAccessTokenId();
    }


}
