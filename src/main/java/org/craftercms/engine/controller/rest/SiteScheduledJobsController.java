/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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

package org.craftercms.engine.controller.rest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.craftercms.core.controller.rest.RestControllerBase;
import org.craftercms.engine.service.context.SiteContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller to access scheduled jobs for the current site.
 *
 * @author joseross
 */
@RestController
@RequestMapping(RestControllerBase.REST_BASE_URI + SiteScheduledJobsController.URL_ROOT)
public class SiteScheduledJobsController extends RestControllerBase {

    public static final String URL_ROOT = "/site/jobs";
    public static final String URL_LIST = "/list";

    @GetMapping(URL_LIST)
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> listScheduledJobs() throws SchedulerException {
        List<Map<String, String>> jobs = new LinkedList<>();
        SiteContext siteContext = SiteContext.getCurrent();
        Scheduler scheduler = siteContext.getScheduler();
        if(scheduler != null) {
            List<String> groups = scheduler.getJobGroupNames();
            for (String group : groups) {
                Set<JobKey> keys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));
                for (JobKey key : keys) {
                    List<Trigger> triggers = (List<Trigger>)scheduler.getTriggersOfJob(key);
                    Map<String, String> job = new HashMap<>();
                    job.put("name", key.getName());
                    job.put("nextFireTime", triggers.get(0).getNextFireTime().toInstant().toString());
                    jobs.add(job);
                }
            }
        }
        return jobs;
    }

}
