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

package org.craftercms.engine.scripting.impl;

import java.util.List;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.engine.util.quartz.JobContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.CronTrigger;
import org.quartz.impl.JobDetailImpl;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfigurationScriptJobResolver}.
 *
 * @author avasquez
 */
public class ConfigurationScriptJobResolverTest {

    @Mock
    private ContentStoreService storeService;
    @Mock
    private SiteContext siteContext;
    private ConfigurationScriptJobResolver resolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        setUpStoreService(storeService);
        setUpSiteContext(siteContext, storeService);

        resolver = new ConfigurationScriptJobResolver();
        resolver.setScriptSuffix(".groovy");
    }

    @Test
    public void testResolveJobs() throws Exception {
        List<JobContext> jobContexts = resolver.resolveJobs(siteContext);

        assertNotNull(jobContexts);
        assertEquals(2, jobContexts.size());

        JobDetailImpl jobDetail = (JobDetailImpl)jobContexts.get(0).getDetail();
        CronTrigger trigger = (CronTrigger)jobContexts.get(0).getTrigger();

        assertEquals(ScriptJob.class, jobDetail.getJobClass());
        assertEquals("/scripts/jobs/morejobs/testJob2.groovy",
                     jobDetail.getJobDataMap().getString(ScriptJob.SCRIPT_URL_DATA_KEY));
        assertEquals("0 0/15 * * * ?", trigger.getCronExpression());

        jobDetail = (JobDetailImpl)jobContexts.get(1).getDetail();
        trigger = (CronTrigger)jobContexts.get(1).getTrigger();

        assertEquals(ScriptJob.class, jobDetail.getJobClass());
        assertEquals("/scripts/jobs/testJob.groovy",
                     jobDetail.getJobDataMap().getString(ScriptJob.SCRIPT_URL_DATA_KEY));
        assertEquals("0 0/15 * * * ?", trigger.getCronExpression());
    }

    private void setUpStoreService(ContentStoreService storeService) {
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);
    }

    private void setUpSiteContext(SiteContext siteContext, ContentStoreService storeService) throws Exception {
        XMLConfiguration config = ConfigUtils.readXmlConfiguration(new ClassPathResource("config/site-config.xml"), ',');
        config.setListDelimiterHandler(new DefaultListDelimiterHandler(','));

        when(siteContext.getSiteName()).thenReturn("default");
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getStoreService()).thenReturn(storeService);
        when(siteContext.getConfig()).thenReturn(config);
    }

}
