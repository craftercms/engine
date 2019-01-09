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
