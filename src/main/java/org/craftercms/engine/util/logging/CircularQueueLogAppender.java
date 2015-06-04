package org.craftercms.engine.util.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.service.context.SiteContext;

/**
 *
 */
public class CircularQueueLogAppender  extends AppenderSkeleton{

    private Buffer buffer; //This has to be sync !!!!
    private static CircularQueueLogAppender instance;
    private int maxQueueSize;

    @Override
    protected void append(final LoggingEvent event) {
        final SiteContext ctx = SiteContext.getCurrent();
        if(ctx!=null) {
            final String siteName = ctx.getSiteName();
            if(StringUtils.isNoneBlank(siteName)){
                buffer.add(subAppend(event,siteName));
            }
        }
    }

    public void setMaxQueueSize(final int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
        if(maxQueueSize<=0){
            throw new IllegalArgumentException("maxQueueSize must be a integer bigger that 0");
        }
        buffer= BufferUtils.synchronizedBuffer(new CircularFifoBuffer(maxQueueSize));
        instance=this;
    }

    @Override
    public void close() {
        buffer.clear();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public static CircularQueueLogAppender loggerQueue(){
        return instance;
    }

    public List<String> getLoggedEvents() {
        final Iterator<Object> iter = buffer.iterator();
        final List<String> str= new ArrayList<>();
        while (iter.hasNext()){
        str.add(String.valueOf(iter.next()));
        }
        return str;
    }

    protected String subAppend(final LoggingEvent event,final String tenantName ) {
        StringBuffer buffer=new StringBuffer( tenantName + "-> "+this.layout.format(event));
        if(layout.ignoresThrowable()) {
            buffer.append(Layout.LINE_SEP);
            String[] s = event.getThrowableStrRep();
            if (s != null) {
                int len = s.length;
                for(int i = 0; i < len; i++) {
                    buffer.append(s[i]);
                    buffer.append(Layout.LINE_SEP);
                }
            }
        }
        return buffer.toString();
    }
}

