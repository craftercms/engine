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

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.craftercms.engine.service.context.SiteContext;

import static org.apache.logging.log4j.core.Appender.ELEMENT_TYPE;
import static org.apache.logging.log4j.core.Core.CATEGORY_NAME;

/**
 *
 */
@Plugin(name = CircularQueueLogAppender.PLUGIN_NAME, category = CATEGORY_NAME, elementType = ELEMENT_TYPE)
public class CircularQueueLogAppender extends AbstractAppender {

    public static final String PLUGIN_NAME = "CircularQueueLogAppender";

    private static Buffer buffer; //This has to be sync !!!!

    private DateTimeFormatter dateFormat;

    private boolean global;

    protected CircularQueueLogAppender(final String name, final Filter filter,
                                       final Layout<? extends Serializable> layout, final boolean ignoreExceptions,
                                       final Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void append(final LogEvent event) {
        String siteName = StringUtils.EMPTY;
        final SiteContext ctx = SiteContext.getCurrent();
        if (ctx != null) {
            siteName = ctx.getSiteName();
        }
        if (global || StringUtils.isNoneBlank(siteName)) {
            Map<String, Object> mappy = new HashMap<>();
            mappy.put("site", siteName);
            mappy.put("level", event.getLevel().toString());
            mappy.put("message", event.getMessage().getFormattedMessage());
            mappy.put("thread", event.getThreadName());
            mappy.put("exception", subAppend(event));
            mappy.put("timestamp", dateFormat.format(Instant.ofEpochMilli(event.getTimeMillis())));
            mappy.put("timestampm", event.getInstant().getEpochMillisecond());
            buffer.add(mappy);
        }
    }

    @Override
    public void stop() {
        super.stop();
        buffer.clear();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getLoggedEvents(final String siteId, final long since) {

        final Iterator<Map<String, Object>> iter = buffer.iterator();
        final List<Map<String, Object>> str = new ArrayList<>();
        while (iter.hasNext()) {
            Map<String, Object> map = iter.next();
            if (map.get("site").toString().equalsIgnoreCase(siteId)) {
                if (Instant.ofEpochMilli((long)map.get("timestampm")).isAfter(Instant.ofEpochMilli(since))) {
                    str.add(map);
                }
            }
        }
        return str;
    }

    protected String subAppend(final LogEvent event) {
        StringBuilder sb = new StringBuilder();
        if(!ignoreExceptions() && !Objects.isNull(event.getThrown())) {
            sb.append(System.lineSeparator());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            event.getThrown().printStackTrace(pw);
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            br.lines().forEach(line -> sb.append(line).append(System.lineSeparator()));
        }
        return sb.toString();
    }

    @PluginFactory
    public static CircularQueueLogAppender createAppender(
        @PluginAttribute(value = "name") String name,
        @PluginElement(value = "Filters") Filter filter,
        @PluginElement(value = "Layout") Layout<? extends Serializable> layout,
        @PluginAttribute(value = "ignoreExceptions") boolean ignoreExceptions,
        @PluginAttribute(value = "maxQueueSize") int maxQueueSize,
        @PluginAttribute(value = "dateFormat") String dateFormat,
        @PluginAttribute(value = "global") boolean global) {

        if (StringUtils.isEmpty(name)) {
            LOGGER.error("No name provided for " + PLUGIN_NAME);
            return null;
        }

        if (Objects.isNull(layout)) {
            layout = PatternLayout.createDefaultLayout();
        }

        if (Objects.isNull(buffer)) {
            LOGGER.debug("Initializing circular log queue buffer");
            if (maxQueueSize <= 0) {
                throw new IllegalArgumentException("maxQueueSize must be a integer bigger that 0");
            }
            buffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(maxQueueSize));
        }

        CircularQueueLogAppender appender = new CircularQueueLogAppender(name, filter, layout, ignoreExceptions, null);
        appender.dateFormat = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.of("UTC"));
        appender.global = global;

        return appender;
    }
}

