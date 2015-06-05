package org.craftercms.engine.util.quartz;

import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * Contains the details for scheduling a Quartz job.
 *
 * @author avasquez
 */
public class JobContext {

    protected JobDetail jobDetail;
    protected Trigger trigger;

    public JobContext(JobDetail jobDetail, Trigger trigger) {
        this.jobDetail = jobDetail;
        this.trigger = trigger;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public Trigger getTrigger() {
        return trigger;
    }

}
