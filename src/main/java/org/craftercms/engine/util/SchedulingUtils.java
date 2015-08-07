package org.craftercms.engine.util;

import java.util.Properties;

import org.craftercms.engine.scripting.impl.ScriptJob;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.quartz.JobContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;

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

    private static final String PROP_THREAD_COUNT = "org.quartz.threadPool.threadCount";
    private static final int DEFAULT_THREAD_COUNT = 10;

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
        String jobName = siteContext.getSiteName() + ":" + scriptUrl;
        JobDetail detail = SchedulingUtils.createScriptJob(siteContext, jobName, scriptUrl);
        Trigger trigger = SchedulingUtils.createCronTrigger("trigger for " + jobName, cronExpression);
        String description = "Job{url='" + scriptUrl + "', cron='" + cronExpression + "'}";

        return new JobContext(detail, trigger, description);
    }

    public static Scheduler createScheduler(String schedulerName) throws SchedulerException {
        Properties props = new Properties();
        props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, schedulerName);
        props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, SimpleThreadPool.class.getName());
        props.setProperty(PROP_THREAD_COUNT, Integer.toString(DEFAULT_THREAD_COUNT));

        SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);

        return schedulerFactory.getScheduler();
    }

}
