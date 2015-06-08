package org.craftercms.engine.scripting.impl;

import java.util.Map;

import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Implementation of a Quartz Job that executes a script.
 *
 * @author avasquez
 */
public class ScriptJob implements Job {

    public static final String SITE_CONTEXT_DATA_KEY = "siteContext";
    public static final String SCRIPT_URL_DATA_KEY = "scriptUrl";
    public static final String SCRIPT_VARIABLES_DATA_KEY = "scriptVariables";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String scriptUrl = dataMap.getString(SCRIPT_URL_DATA_KEY);
        Map<String, Object> scriptVariables = (Map<String, Object>)dataMap.get(SCRIPT_VARIABLES_DATA_KEY);
        SiteContext siteContext = (SiteContext)dataMap.get(SITE_CONTEXT_DATA_KEY);
        ScriptFactory scriptFactory = siteContext.getScriptFactory();

        if (scriptFactory == null) {
            throw new JobExecutionException(
                "No script factory associate to site context '" + siteContext.getSiteName() + "'");
        }

        try {
            scriptFactory.getScript(scriptUrl).execute(scriptVariables);
        } catch (ScriptException e) {
            throw new JobExecutionException("Error executing script job at " + scriptUrl, e);
        }
    }

}
