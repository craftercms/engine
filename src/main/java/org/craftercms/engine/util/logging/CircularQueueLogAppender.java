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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.craftercms.engine.service.context.SiteContext;

/**
 *
 */
public class CircularQueueLogAppender extends AppenderSkeleton {

    private Buffer buffer; //This has to be sync !!!!
    private static CircularQueueLogAppender instance;
    private int maxQueueSize;
    private SimpleDateFormat dateFormat;
    private String dateFormatString;

    @Override
    @SuppressWarnings("unchecked")
    protected void append(final LoggingEvent event) {
        final SiteContext ctx = SiteContext.getCurrent();
        if (ctx != null) {
            final String siteName = ctx.getSiteName();
            if (StringUtils.isNoneBlank(siteName)) {
                Map<String, Object> mappy = new HashMap<>();
                mappy.put("site", siteName);
                mappy.put("level", event.getLevel().toString());
                mappy.put("message", event.getRenderedMessage());
                mappy.put("thread", event.getThreadName());
                mappy.put("exception", subAppend(event));
                mappy.put("timestamp", dateFormat.format(new Date(event.getTimeStamp())));
                mappy.put("timestampm", event.getTimeStamp());
                buffer.add(mappy);
            }
        }
    }

    public void setMaxQueueSize(final int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
        if (maxQueueSize <= 0) {
            throw new IllegalArgumentException("maxQueueSize must be a integer bigger that 0");
        }
        buffer = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(maxQueueSize));
        instance = this;
        dateFormat = new SimpleDateFormat(dateFormatString);
    }

    @Override
    public void close() {
        buffer.clear();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public void setDateFormat(final String dateFormat) {
        this.dateFormatString = dateFormat;
    }

    public static CircularQueueLogAppender loggerQueue() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public List<HashMap<String, Object>> getLoggedEvents(final String siteId, final long since) {

        final Iterator<HashMap<String, Object>> iter = buffer.iterator();
        final List<HashMap<String, Object>> str = new ArrayList<>();
        while (iter.hasNext()) {
            HashMap<String, Object> map = iter.next();
            if (map.get("site").toString().equalsIgnoreCase(siteId)) {
                if (new Date((long)map.get("timestampm")).after(new Date(since))) {
                    str.add(map);
                }
            }
        }
        return str;
    }

    protected String subAppend(final LoggingEvent event) {
        StringBuffer buffer = new StringBuffer();
        if (layout.ignoresThrowable()) {
            buffer.append(Layout.LINE_SEP);
            String[] s = event.getThrowableStrRep();
            if (s != null) {
                int len = s.length;
                for (int i = 0; i < len; i++) {
                    buffer.append(s[i]);
                    buffer.append(Layout.LINE_SEP);
                }
            }
        }
        return buffer.toString();
    }
}

