package org.craftercms.engine.scripting.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.test.utils.ContentStoreServiceMockUtils;
import org.craftercms.engine.util.groovy.ContentStoreResourceConnector;
import org.craftercms.engine.util.quartz.JobContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.CronTrigger;
import org.quartz.impl.JobDetailImpl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
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
    private SiteContext context;
    private FolderBasedScriptJobResolver resolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        setUpStoreService(storeService);

        context = createSiteContext(storeService);

        resolver = new FolderBasedScriptJobResolver();
        resolver.setFolderUrl("/scripts/jobs");
        resolver.setCronExpression(HOURLY_CRON_EXPRESSION);
    }

    @Test
    public void testResolveJobs() throws Exception {
        List<JobContext> jobContexts = resolver.resolveJobs(context);

        assertNotNull(jobContexts);
        assertEquals(1, jobContexts.size());

        JobDetailImpl jobDetail = (JobDetailImpl)jobContexts.get(0).getJobDetail();
        CronTrigger trigger = (CronTrigger)jobContexts.get(0).getTrigger();

        assertEquals(ScriptJob.class, jobDetail.getJobClass());
        assertEquals("/scripts/jobs/testJob.groovy",
                     jobDetail.getJobDataMap().getString(ScriptJob.SCRIPT_URL_DATA_KEY));
        assertEquals(HOURLY_CRON_EXPRESSION, trigger.getCronExpression());
    }

    private void setUpStoreService(ContentStoreService storeService) {
        ContentStoreServiceMockUtils.setUpGetContentFromClassPath(storeService);

        when(storeService.findChildren(any(Context.class), anyString())).then(new Answer<List<Item>>() {

            @Override
            public List<Item> answer(InvocationOnMock invocation) throws Throwable {
                String folderUrl = (String)invocation.getArguments()[1];
                Resource folderRes = new ClassPathResource(folderUrl);
                File folder = folderRes.getFile();
                String[] childNames = folder.list();
                List<Item> children = new ArrayList<>(childNames.length);

                for (String childName : childNames) {
                    Item child = new Item();
                    child.setUrl(folderUrl + "/" + childName);

                    children.add(child);
                }

                return children;
            }

        });
    }

    private SiteContext createSiteContext(ContentStoreService storeService) throws Exception {
        SiteContext siteContext = mock(SiteContext.class);
        ScriptFactory scriptFactory = createScriptFactory(siteContext);

        when(siteContext.getSiteName()).thenReturn("default");
        when(siteContext.getContext()).thenReturn(mock(Context.class));
        when(siteContext.getStoreService()).thenReturn(storeService);
        when(siteContext.getScriptFactory()).thenReturn(scriptFactory);

        return siteContext;
    }

    private ScriptFactory createScriptFactory(SiteContext context) {
        ContentStoreResourceConnector resourceConnector = new ContentStoreResourceConnector(context);

        return new GroovyScriptFactory(resourceConnector, Collections.<String, Object>emptyMap());
    }

}
