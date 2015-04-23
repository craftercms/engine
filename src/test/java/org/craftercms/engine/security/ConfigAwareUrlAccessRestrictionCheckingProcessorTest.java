package org.craftercms.engine.security;

import java.util.Collections;

import org.bson.types.ObjectId;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.test.utils.CacheTemplateMockUtils;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.impl.DefaultAuthentication;
import org.craftercms.security.exception.AccessDeniedException;
import org.craftercms.security.processors.RequestSecurityProcessorChain;
import org.craftercms.security.utils.SecurityUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link org.craftercms.engine.security.ConfigAwareUrlAccessRestrictionCheckingProcessor}.
 *
 * @author avasquez
 */
public class ConfigAwareUrlAccessRestrictionCheckingProcessorTest extends ConfigAwareTestBase {

    private ConfigAwareUrlAccessRestrictionCheckingProcessor processor;
    @Mock
    private CacheTemplate cacheTemplate;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        CacheTemplateMockUtils.setUpWithNoCaching(cacheTemplate);

        processor = new ConfigAwareUrlAccessRestrictionCheckingProcessor();
        processor.setCacheTemplate(cacheTemplate);
        processor.setUrlRestrictions(Collections.singletonMap("/*", "permitAll()"));
    }

    @Test(expected = AccessDeniedException.class)
    public void testProcessRequest() throws Exception {
        RequestContext requestContext = RequestContext.getCurrent();
        RequestSecurityProcessorChain chain = mock(RequestSecurityProcessorChain.class);

        SecurityUtils.setAuthentication(requestContext.getRequest(),
                                        new DefaultAuthentication(ObjectId.get().toString(), new Profile()));

        processor.processRequest(requestContext, chain);
    }

}
