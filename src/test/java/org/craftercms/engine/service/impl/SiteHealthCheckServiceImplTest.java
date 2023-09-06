/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.service.impl;

import org.craftercms.engine.service.context.SiteListResolver;
import org.craftercms.engine.service.health.HealthCheck;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SiteHealthCheckServiceImplTest {

    private static final String SITE1 = "site1";
    private static final String SITE2 = "site2";
    private static final String SITE3 = "site3";

    @Mock
    SiteListResolver siteListResolver;
    @Mock
    HealthCheck healthCheck1;
    @Mock
    HealthCheck healthCheck2;
    @Spy
    ArrayList<HealthCheck> healthChecks;

    @InjectMocks
    SiteHealthCheckServiceImpl siteHealthCheckService;

    @Before
    public void setUp() throws Exception {
        healthChecks.add(healthCheck1);
        healthChecks.add(healthCheck2);
    }

    @Test
    public void noSiteContextTest() {
        assertTrue("Health check should return true if sites is empty", siteHealthCheckService.healthCheck());
    }

    @Test
    public void oneSiteInvalidTest() {
        when(healthChecks.get(0).checkHealth(SITE1)).thenReturn(true);
        when(healthChecks.get(1).checkHealth(SITE1)).thenReturn(false);
        when(siteListResolver.getSiteList()).thenReturn(List.of(SITE1));

        assertFalse("Health check should return false if all sites are invalid", siteHealthCheckService.healthCheck());
    }

    @Test
    public void oneSiteValidTest() {
        when(healthChecks.get(0).checkHealth(SITE1)).thenReturn(true);
        when(healthChecks.get(1).checkHealth(SITE1)).thenReturn(true);
        when(siteListResolver.getSiteList()).thenReturn(List.of(SITE1));

        assertTrue("Health check should return true if all checks pass for at least one site", siteHealthCheckService.healthCheck());
    }

    @Test
    public void twoSitesOneValidTest() {
        when(healthChecks.get(0).checkHealth(SITE1)).thenReturn(true);
        when(healthChecks.get(1).checkHealth(SITE1)).thenReturn(true);
        lenient().when(healthChecks.get(0).checkHealth(SITE2)).thenReturn(false);
        lenient().when(healthChecks.get(1).checkHealth(SITE2)).thenReturn(false);
        when(siteListResolver.getSiteList()).thenReturn(List.of(SITE1, SITE2));

        assertTrue("Health check should return true if all checks pass for at least one site", siteHealthCheckService.healthCheck());
    }

    @Test
    public void noSiteIsValidTest() {
        when(healthChecks.get(0).checkHealth(anyString())).thenReturn(false);
        lenient().when(healthChecks.get(1).checkHealth(anyString())).thenReturn(false);
        when(siteListResolver.getSiteList()).thenReturn(List.of(SITE1, SITE2, SITE3));

        assertFalse("Health check should return false if no site passes the checks", siteHealthCheckService.healthCheck());
    }

    @Test
    public void firstCheckFailsTest() {
        // If a check fails, there is no need to execute the others
        when(healthChecks.get(0).checkHealth(SITE1)).thenReturn(false);
        when(healthChecks.get(1).checkHealth(SITE1)).thenReturn(true);
        when(siteListResolver.getSiteList()).thenReturn(List.of(SITE1));

        assertFalse("Health check should return false if there are no sites passing the checks", siteHealthCheckService.healthCheck());
        verifyNoInteractions(healthChecks.get(1));
    }
}
