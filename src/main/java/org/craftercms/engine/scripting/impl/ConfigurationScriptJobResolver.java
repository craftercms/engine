package org.craftercms.engine.scripting.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.exception.SchedulingException;
import org.craftercms.engine.scripting.ScriptJobResolver;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ContentStoreUtils;
import org.craftercms.engine.util.SchedulingUtils;
import org.craftercms.engine.util.quartz.JobContext;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link ScriptJobResolver} that resolves job based on configuration mappings, like the following:
 *
 * <pre>
 *     &lt;jobs&gt;
 *         &lt;jobFolder&gt;
 *             &lt;path&gt;/scripts/jobs/every15mins&lt;/path&gt;
 *             &lt;cronExpression&gt;0 0/15 * * * ?&lt;/cronExpression&gt;
 *         &lt;/jobFolder&gt;
 *         &lt;job&gt;
 *             &lt;path&gt;/scripts/jobs/myJob.groovy&lt;/path&gt;
 *             &lt;cronExpression&gt;0 0/15 * * * ?&lt;/cronExpression&gt;
 *         &lt;/job&gt;
 *     &lt;/jobs&gt;
 * </pre>
 */
public class ConfigurationScriptJobResolver implements ScriptJobResolver {

    public static final String JOB_FOLDER_KEY = "jobs.jobFolder";
    public static final String JOB_KEY = "jobs.job";
    public static final String PATH_KEY = "path";
    public static final String CRON_EXPRESSION_KEY = "cronExpression";

    protected String scriptSuffix;

    @Required
    public void setScriptSuffix(String scriptSuffix) {
        this.scriptSuffix = scriptSuffix;
    }

    @Override
    public List<JobContext> resolveJobs(SiteContext siteContext) throws SchedulingException {
        HierarchicalConfiguration config = siteContext.getConfig();
        List<JobContext> jobContexts = null;

        if (config != null) {
            List<HierarchicalConfiguration> jobFoldersConfig = config.configurationsAt(JOB_FOLDER_KEY);
            if (CollectionUtils.isNotEmpty(jobFoldersConfig)) {
                for (HierarchicalConfiguration jobFolderConfig : jobFoldersConfig) {
                    jobContexts = getJobsUnderFolder(siteContext, jobFolderConfig);
                }
            }

            List<HierarchicalConfiguration> jobsConfig = config.configurationsAt(JOB_KEY);
            if (CollectionUtils.isNotEmpty(jobsConfig)) {
                for (HierarchicalConfiguration jobConfig : jobsConfig) {
                    JobContext jobContext = getJob(siteContext, jobConfig);
                    if (jobContext != null) {
                        if (jobContexts == null) {
                            jobContexts = new ArrayList<>();
                        }

                        jobContexts.add(jobContext);
                    }
                }
            }
        }

        return jobContexts;
    }

    protected List<JobContext> getJobsUnderFolder(SiteContext siteContext, HierarchicalConfiguration jobFolderConfig) {
        List<JobContext> jobContexts = null;
        String folderPath = jobFolderConfig.getString(PATH_KEY);
        String cronExpression = jobFolderConfig.getString(CRON_EXPRESSION_KEY);
        ContentStoreService storeService = siteContext.getStoreService();
        Context context = siteContext.getContext();

        if (StringUtils.isNotEmpty(folderPath) && StringUtils.isNotEmpty(cronExpression)) {
            List<String> scriptPaths = ContentStoreUtils.findChildrenUrl(storeService, context, folderPath);
            if (CollectionUtils.isNotEmpty(scriptPaths)) {
                for (String scriptPath : scriptPaths) {
                    if (scriptPath.endsWith(scriptSuffix)) {
                        if (jobContexts == null) {
                            jobContexts = new ArrayList<>();
                        }

                        jobContexts.add(SchedulingUtils.createJobContext(siteContext, scriptPath, cronExpression));
                    }
                }
            }
        }

        return jobContexts;
    }

    protected JobContext getJob(SiteContext siteContext, HierarchicalConfiguration jobConfig) {
        String scriptPath = jobConfig.getString(PATH_KEY);
        String cronExpression = jobConfig.getString(CRON_EXPRESSION_KEY);

        if (StringUtils.isNotEmpty(scriptPath) && StringUtils.isNotEmpty(cronExpression)) {
            if (siteContext.getStoreService().exists(siteContext.getContext(), scriptPath)) {
                return SchedulingUtils.createJobContext(siteContext, scriptPath, cronExpression);
            } else {
                throw new SchedulingException("Script job " + scriptPath + " for site '" + siteContext.getSiteName() +
                                              "' not found");
            }
        } else {
            return null;
        }
    }

}
