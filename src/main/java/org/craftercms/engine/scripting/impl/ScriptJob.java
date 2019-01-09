/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.scripting.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.GroovyScriptUtils;
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
    public static final String SERVLET_CONTEXT_DATA_KEY = "servletContext";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String scriptUrl = dataMap.getString(SCRIPT_URL_DATA_KEY);
        SiteContext siteContext = (SiteContext)dataMap.get(SITE_CONTEXT_DATA_KEY);
        ServletContext servletContext = (ServletContext)dataMap.get(SERVLET_CONTEXT_DATA_KEY);
        ScriptFactory scriptFactory = siteContext.getScriptFactory();

        if (scriptFactory == null) {
            throw new JobExecutionException(
                "No script factory associate to site context '" + siteContext.getSiteName() + "'");
        }

        SiteContext.setCurrent(siteContext);
        try {
            Map<String, Object> variables = new HashMap<>();
            GroovyScriptUtils.addJobScriptVariables(variables, servletContext);

            scriptFactory.getScript(scriptUrl).execute(variables);
        } catch (Exception e) {
            throw new JobExecutionException("Error executing script job at " + scriptUrl, e);
        } finally {
            SiteContext.clear();
        }
    }

}
