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
package org.craftercms.engine.util;

import java.util.concurrent.Executor;

import javax.servlet.ServletContext;

import org.craftercms.engine.scripting.impl.ScriptJob;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.quartz.JobContext;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import static org.craftercms.engine.scripting.impl.ScriptJob.*;
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

    public static JobDetail createScriptJob(SiteContext siteContext, String jobName, String scriptUrl,
                                            ServletContext servletContext) {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(SITE_CONTEXT_DATA_KEY, siteContext);
        dataMap.put(SCRIPT_URL_DATA_KEY, scriptUrl);
        dataMap.put(SERVLET_CONTEXT_DATA_KEY, servletContext);

        JobDetail job = newJob(ScriptJob.class)
            .withIdentity(jobName)
            .setJobData(dataMap)
            .build();

        return job;
    }

    public static JobContext createJobContext(SiteContext siteContext, String scriptUrl, String cronExpression,
                                              ServletContext servletContext) {
        String jobName = siteContext.getSiteName() + ":" + scriptUrl;
        JobDetail detail = SchedulingUtils.createScriptJob(siteContext, jobName, scriptUrl, servletContext);
        Trigger trigger = SchedulingUtils.createCronTrigger("trigger for " + jobName, cronExpression);
        String description = "Job{url='" + scriptUrl + "', cron='" + cronExpression + "'}";

        return new JobContext(detail, trigger, description);
    }

    public static Scheduler createScheduler(String schedulerName, Executor threaPoolExecutor) throws SchedulerException {
        try {
            SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
            schedulerFactoryBean.setSchedulerName(schedulerName);
            schedulerFactoryBean.setTaskExecutor(threaPoolExecutor);
            schedulerFactoryBean.afterPropertiesSet();

            return schedulerFactoryBean.getObject();
        } catch (Exception e) {
            throw new SchedulerException("Unable to create scheduler", e);
        }
    }

}
