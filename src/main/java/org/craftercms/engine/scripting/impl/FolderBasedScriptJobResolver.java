package org.craftercms.engine.scripting.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.engine.exception.SchedulingException;
import org.craftercms.engine.scripting.ScriptJobResolver;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ContentStoreUtils;
import org.craftercms.engine.util.SchedulingUtils;
import org.craftercms.engine.util.quartz.JobContext;
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
    protected String scriptSuffix;

    @Required
    public void setFolderUrl(String folderUrl) {
        this.folderUrl = folderUrl;
    }

    @Required
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Required
    public void setScriptSuffix(String scriptSuffix) {
        this.scriptSuffix = scriptSuffix;
    }

    @Override
    public List<JobContext> resolveJobs(SiteContext siteContext) throws SchedulingException {
        List<String> scriptUrls = ContentStoreUtils.findChildrenUrl(siteContext.getStoreService(),
                                                                    siteContext.getContext(), folderUrl);
        List<JobContext> jobContexts = null;

        if (CollectionUtils.isNotEmpty(scriptUrls)) {
            for (String scriptUrl : scriptUrls) {
                if (scriptUrl.endsWith(scriptSuffix)) {
                    if (jobContexts == null) {
                        jobContexts = new ArrayList<>();
                    }

                    jobContexts.add(SchedulingUtils.createJobContext(siteContext, scriptUrl, cronExpression));
                }
            }
        }

        return jobContexts;
    }

}
