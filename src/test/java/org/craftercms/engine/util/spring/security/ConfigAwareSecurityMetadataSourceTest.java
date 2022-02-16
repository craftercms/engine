/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.engine.util.spring.security;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.test.utils.CacheTemplateMockUtils;
import org.craftercms.engine.test.utils.ConfigAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfigAwareSecurityMetadataSource}.
 *
 * @author avasquez
 */
public class ConfigAwareSecurityMetadataSourceTest extends ConfigAwareTestBase {

    private ConfigAwareSecurityMetadataSource metadataSource;
    @Mock
    private CacheTemplate cacheTemplate;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        CacheTemplateMockUtils.setUpWithNoCaching(cacheTemplate);

        metadataSource = new ConfigAwareSecurityMetadataSource(cacheTemplate);
    }

    @Test
    public void testProcessRequest() {
        HttpServletRequest request = RequestContext.getCurrent().getRequest();

        FilterInvocation invocation = mock(FilterInvocation.class);
        when(invocation.getRequest()).thenReturn(request);

        Collection<ConfigAttribute> attributes = metadataSource.getAttributes(invocation);

        assertThat(attributes, notNullValue());
        assertThat(attributes.size(), is(1));
    }

}
