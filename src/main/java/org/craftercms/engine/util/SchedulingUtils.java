package org.craftercms.engine.util;

import org.craftercms.engine.scripting.impl.ScriptJob;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.quartz.JobContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import static org.craftercms.engine.scripting.impl.ScriptJob.SCRIPT_URL_DATA_KEY;
import static org.craftercms.engine.scripting.impl.ScriptJob.SITE_CONTEXT_DATA_KEY;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Utility methods for scheduling.
 *
 * @author avasquez
 */
public class SchedulingUtils {

    private SchedulingUtils() {
    }

    public static Trigger createCronTrigger(String triggerName, String cronExpression) {
        Trigger trigger = newTrigger()
            .withIdentity(triggerName)
            .withSchedule(cronSchedule(cronExpression))
            .build();

        return trigger;
    }

    public static JobDetail createScriptJob(SiteContext siteContext, String jobName, String scriptUrl) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(SITE_CONTEXT_DATA_KEY, siteContext);
        dataMap.put(SCRIPT_URL_DATA_KEY, scriptUrl);

        JobDetail job = newJob(ScriptJob.class)
            .withIdentity(jobName)
            .setJobData(dataMap)
            .build();

        return job;
    }

    public static JobContext createJobContext(SiteContext siteContext, String scriptUrl, String cronExpression) {
        JobDetail jobDetail = SchedulingUtils.createScriptJob(siteContext, scriptUrl, scriptUrl);
        Trigger trigger = SchedulingUtils.createCronTrigger("trigger for " + scriptUrl, cronExpression);

        return new JobContext(jobDetail, trigger);
    }

}
