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
package org.craftercms.engine.event;

import org.craftercms.engine.service.context.SiteContext;
import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletRequest;

/**
 * Application event that is related to a site.
 *
 * @author avasquez
 */
public class SiteEvent extends ApplicationEvent {

    /**
     * Returns the latest event of the specified class that has been fired during the handling of the request.
     *
     * @param eventClass the event class
     * @param request the request
     *
     * @return the latest request event
     */
    public static SiteEvent getLatestRequestEvent(Class<? extends SiteEvent> eventClass,
                                                  HttpServletRequest request) {
        return (SiteEvent) request.getAttribute(eventClass.getName());
    }

    /**
     * Create a new ApplicationEvent.
     *
     * @param siteContext the site's context
     */
    public SiteEvent(SiteContext siteContext) {
        super(siteContext);
    }

    public SiteContext getSiteContext() {
        return (SiteContext) getSource();
    }

}
