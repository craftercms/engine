/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
