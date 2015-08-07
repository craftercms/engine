package org.craftercms.engine.util.quartz;

import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * Contains the details for scheduling a Quartz job.
 *
 * @author avasquez
 */
public class JobContext {

    protected JobDetail detail;
    protected Trigger trigger;
    protected String description;

    public JobContext(JobDetail detail, Trigger trigger, String description) {
        this.detail = detail;
        this.trigger = trigger;
        this.description = description;
    }

    public JobDetail getDetail() {
        return detail;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

}
