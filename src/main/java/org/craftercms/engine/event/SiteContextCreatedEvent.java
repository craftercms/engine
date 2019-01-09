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

/**
 * Event published when a new {@link SiteContext} is created.
 *
 * @author avasquez
 */
public class SiteContextCreatedEvent extends ApplicationEvent {

    protected SiteContext siteContext;

    /**
     * Create a new event.
     *
     * @param siteContext   the SiteContext created
     * @param source        the component that published the event (never {@code null})
     */
    public SiteContextCreatedEvent(SiteContext siteContext, Object source) {
        super(source);

        this.siteContext = siteContext;
    }

    public SiteContext getSiteContext() {
        return siteContext;
    }

}
