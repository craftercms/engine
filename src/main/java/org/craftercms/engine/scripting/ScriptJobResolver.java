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
     * @param siteContext the context used to look up for the scripts
     *
     * @return the details of the jobs to be scheduled
     */
    List<JobContext> resolveJobs(SiteContext siteContext) throws SchedulingException;

}
