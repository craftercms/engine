package org.craftercms.engine.scripting;

import java.util.List;

import org.craftercms.engine.exception.SchedulingException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.quartz.JobContext;

/**
 * Resolves any number of script jobs to be scheduled and executed.
 *
 * @author avasquez
 */
public interface ScriptJobResolver {

    /**
     * Resolves any number of script jobs to be scheduled and executed.
     *
     * @param context   the context used to look up for the scripts
     *
     * @return the details of the jobs to be scheduled
     */
    List<JobContext> resolveJobs(SiteContext context) throws SchedulingException;

}
