package org.craftercms.engine.scripting.impl;

import java.util.List;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.craftercms.engine.util.quartz.JobContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.CronTrigger;
import org.quartz.impl.JobDetailImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FolderBasedScriptJobResolver}.
 *
 * @author avasquez
 */
public class FolderBasedScriptJobResolverTest {

    private static final String HOURLY_CRON_EXPRESSION = "0 0 * * * ?";

    @Mock
    private ContentStoreService storeService;
    @Mock
    private SiteContext siteContext;
    private FolderBasedScriptJobResolver resolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        setUpStoreService(storeService);
        setUpSiteContext(siteContext, storeService);

        resolver = new FolderBasedScriptJobResolver();
        resolver.setFolderUrl("/scripts/jobs");
        resolver.setCronExpression(HOURLY_CRON_EXPRESSION);
        resolver.setScriptSuffix(".groovy");
    }

    @Test
    public void testResolveJobs() throws Exception {
        List<JobContext> jobContexts = resolver.resolveJobs(siteContext);

        assertNotNull(jobContexts);
        assertEquals(1, jobContexts.size());

        JobDetailImpl jobDetail = (JobDetailImpl)jobContexts.get(0).getDetail();
        CronTrigger trigger = (CronTrigger)jobContexts.get(0).getTrigger();

        assertEquals(ScriptJob.class, jobDetail.getJobClass());
        assertEquals("/scripts/jobs/testJob.groovy",
                     jobDetail.getJobDataMap().getString(ScriptJob.SCRIPT_URL_DATA_KEY));
        assertEquals(HOURLY_CRON_EXPRESSION, trigger.getCronExpression());
    }

    private void setUpStoreService(ContentStoreService storeService) {
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);
    }

    private void setUpSiteContext(SiteContext siteContext, ContentStoreService storeService) throws Exception {
        when(siteContext.getSiteName()).thenReturn("default");
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getStoreService()).thenReturn(storeService);
    }

}
