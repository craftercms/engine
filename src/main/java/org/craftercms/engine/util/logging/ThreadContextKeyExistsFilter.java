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

package org.craftercms.engine.util.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import static org.apache.logging.log4j.core.Filter.ELEMENT_TYPE;
import static org.apache.logging.log4j.core.Core.CATEGORY_NAME;

/**
 * Log4j filter that decides to log an entry based on whether an MDC key exists or not. If the key specified by
 * {@code key} exists, and {@code acceptIfKeyExists} is true, then
 * {@link org.apache.logging.log4j.core.Filter.Result#ACCEPT} is returned. If
 * {@code acceptIfKeyExists} is false, {@link org.apache.logging.log4j.core.Filter.Result#DENY} is returned. If the
 * key doesn't exist, and {@code denyIfKeyDoesNotExist} is true, then
 * {@link org.apache.logging.log4j.core.Filter.Result#DENY} is returned, otherwise
 * {@link org.apache.logging.log4j.core.Filter.Result#NEUTRAL} is
 * returned.
 *
 * @author avasquez
 */
@Plugin(name = ThreadContextKeyExistsFilter.PLUGIN_NAME, category = CATEGORY_NAME, elementType = ELEMENT_TYPE)
public class ThreadContextKeyExistsFilter extends AbstractFilter {

    public static final String PLUGIN_NAME = "ThreadContextKeyExistsFilter";

    private String key;
    private boolean acceptIfKeyExists;
    private boolean denyIfKeyDoesNotExist;

    public void setKey(String key) {
        this.key = key;
    }

    public void setAcceptIfKeyExists(boolean acceptIfKeyExists) {
        this.acceptIfKeyExists = acceptIfKeyExists;
    }

    public void setDenyIfKeyDoesNotExist(boolean denyIfKeyDoesNotExist) {
        this.denyIfKeyDoesNotExist = denyIfKeyDoesNotExist;
    }

    @Override
    public Result filter(final LogEvent event) {
        return filter();
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
                         final Throwable t) {
        return filter();
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
                         final Throwable t) {
        return filter();
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object... params) {
        return filter();
    }

    public Result filter() {
        if (key == null) {
            return Result.NEUTRAL;
        }

        if (ThreadContext.get(key) != null) {
            if (acceptIfKeyExists) {
                return Result.ACCEPT;
            } else {
                return Result.DENY;
            }
        } else {
            if (denyIfKeyDoesNotExist) {
                return Result.DENY;
            } else {
                return Result.NEUTRAL;
            }
        }
    }

    @PluginFactory
    public static ThreadContextKeyExistsFilter createFilter(
        @PluginAttribute(value = "key") String key,
        @PluginAttribute(value = "acceptIfKeyExists") boolean acceptIfKeyExists,
        @PluginAttribute(value = "denyIfKeyDoesNotExist") boolean denyIfKeyDoesNotExist) {
        ThreadContextKeyExistsFilter filter = new ThreadContextKeyExistsFilter();
        filter.setKey(key);
        filter.setAcceptIfKeyExists(acceptIfKeyExists);
        filter.setDenyIfKeyDoesNotExist(denyIfKeyDoesNotExist);
        return filter;
    }

}
