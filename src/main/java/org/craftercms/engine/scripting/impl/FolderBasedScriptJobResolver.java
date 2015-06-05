package org.craftercms.engine.scripting.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.engine.exception.SchedulingException;
import org.craftercms.engine.scripting.ScriptJobResolver;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.GroovyUtils;
import org.craftercms.engine.util.SchedulingUtils;
import org.craftercms.engine.util.quartz.JobContext;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Required;

/**
 * Folder based {@link ScriptJobResolver}, which resolves all scripts under a certain folder, and creates a trigger
 * to run them using a specific cron expression. For example, a resolver for the folder name daily can return scripts
 * that need to be run every day at 12:00 am.
 *
 * @author avasquez
 */
public class FolderBasedScriptJobResolver implements ScriptJobResolver {

    protected String folderUrl;
    protected String cronExpression;

    @Required
    public void setFolderUrl(String folderUrl) {
        this.folderUrl = folderUrl;
    }

    @Required
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public List<JobContext> resolveJobs(SiteContext context) throws SchedulingException {
        List<String> scriptUrls = findScripts(context);
        List<JobContext> jobContexts = null;

        if (CollectionUtils.isNotEmpty(scriptUrls)) {
            for (String scriptUrl : scriptUrls) {
                Map<String, Object> scriptVariables = new HashMap<>();
                scriptVariables.put(GroovyUtils.VARIABLE_LOGGER, GroovyUtils.LOGGER);

                JobDetail jobDetail = SchedulingUtils.createScriptJob(scriptUrl, context, scriptUrl, scriptVariables);
                Trigger trigger = SchedulingUtils.createCronTrigger("trigger for " + scriptUrl, cronExpression);

                if (jobContexts == null) {
                    jobContexts = new ArrayList<>();
                }

                jobContexts.add(new JobContext(jobDetail, trigger));
            }
        }

        return jobContexts;
    }

    protected List<String> findScripts(SiteContext context) {
        ContentStoreService storeService = context.getStoreService();
        List<Item> scriptItems = storeService.findChildren(context.getContext(), folderUrl);
        List<String> scriptUrls = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(scriptItems)) {
            for (Item scriptItem : scriptItems) {
                scriptUrls.add(scriptItem.getUrl());
            }
        }

        return scriptUrls;
    }

}
