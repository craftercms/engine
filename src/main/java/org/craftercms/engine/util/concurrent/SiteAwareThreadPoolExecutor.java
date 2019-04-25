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

package org.craftercms.engine.util.concurrent;

import java.util.concurrent.Executor;

import org.craftercms.engine.service.context.SiteContext;

/**
 * Utility class that wraps a {@link Executor} object to support {@link SiteContext} in reusable threads
 *
 * @author joseross
 * @since 3.1
 */
public class SiteAwareThreadPoolExecutor implements Executor {

    /**
     * The {@link SiteContext} to use for all threads
     */
    protected SiteContext siteContext;

    /**
     * The actual {@link Executor} to use
     */
    protected Executor wrappedExecutor;

    public SiteAwareThreadPoolExecutor(final SiteContext siteContext, final Executor wrappedExecutor) {
        this.siteContext = siteContext;
        this.wrappedExecutor = wrappedExecutor;
    }

    @Override
    public void execute(final Runnable command) {
        wrappedExecutor.execute(new SiteAwareRunnable(siteContext, command));
    }

}
