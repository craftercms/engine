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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.engine.exception.SchedulingException;
import org.craftercms.engine.scripting.ScriptJobResolver;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ContentStoreUtils;
import org.craftercms.engine.util.SchedulingUtils;
import org.craftercms.engine.util.quartz.JobContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.ServletContextAware;

/**
 * Folder based {@link ScriptJobResolver}, which resolves all scripts under a certain folder, and creates a trigger
 * to run them using a specific cron expression. For example, a resolver for the folder name daily can return scripts
 * that need to be run every day at 12:00 am.
 *
 * @author avasquez
 */
public class FolderBasedScriptJobResolver implements ScriptJobResolver, ServletContextAware {

    protected String folderUrl;
    protected String cronExpression;
    protected String scriptSuffix;
    protected ServletContext servletContext;

    @Required
    public void setFolderUrl(String folderUrl) {
        this.folderUrl = folderUrl;
    }

    @Required
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Required
    public void setScriptSuffix(String scriptSuffix) {
        this.scriptSuffix = scriptSuffix;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public List<JobContext> resolveJobs(SiteContext siteContext) throws SchedulingException {
        List<String> scriptUrls = ContentStoreUtils.findChildrenUrl(siteContext.getStoreService(),
                                                                    siteContext.getContext(), folderUrl);
        List<JobContext> jobContexts = null;

        if (CollectionUtils.isNotEmpty(scriptUrls)) {
            for (String scriptUrl : scriptUrls) {
                if (scriptUrl.endsWith(scriptSuffix)) {
                    if (jobContexts == null) {
                        jobContexts = new ArrayList<>();
                    }

                    jobContexts.add(SchedulingUtils.createJobContext(siteContext, scriptUrl, cronExpression, servletContext));
                }
            }
        }

        return jobContexts;
    }

}
