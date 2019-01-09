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

package org.craftercms.engine.util.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.craftercms.core.service.Content;

/**
 * Implementation of {@link java.net.URLConnection} that wraps a {@link org.craftercms.core.service.Content}.
 *
 * @author Alfonso Vásquez
 */
public class ContentStoreUrlConnection extends URLConnection {

    private static final String CONTENT_LENGTH = "content-length";
    private static final String CONTENT_TYPE = "content-type";
    private static final String LAST_MODIFIED = "last-modified";
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String TIMEZONE = "GMT";

    protected Content content;
    protected Map<String, String> headers;
    protected String contentType;
    protected long length;
    protected long lastModified;
    protected InputStream is;

    protected boolean connected;
    protected boolean initializedHeaders;

    public ContentStoreUrlConnection(URL url, Content content) {
        super(url);

        this.content = content;
        this.headers = new LinkedHashMap<>();
    }

    @Override
    public void connect() throws IOException {
        if (!connected) {
            is = content.getInputStream();
            connected = true;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();

        return is;
    }

    protected void initializeHeaders() {
        if (!initializedHeaders) {
            length = content.getLength();
            lastModified = content.getLastModified();

            FileNameMap map = getFileNameMap();
            contentType = map.getContentTypeFor(url.getFile());

            if (contentType != null) {
                headers.put(CONTENT_TYPE, contentType);
            }

            headers.put(CONTENT_LENGTH, String.valueOf(length));

            /*
             * Format the last-modified field into the preferred
             * Internet standard - ie: fixed-length subset of that
             * defined by RFC 1123
             * */
            Date date = new Date(lastModified);
            SimpleDateFormat dateFormat = new SimpleDateFormat (DATE_FORMAT, Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

            headers.put(LAST_MODIFIED, dateFormat.format(date));

            initializedHeaders = true;
        }
    }

    @Override
    public String getHeaderField(String name) {
        initializeHeaders();

        return headers.get(name);
    }

    @Override
    public String getHeaderField(int n) {
        initializeHeaders();

        Collection<String> values = headers.values();
        String[] valuesArray = values.toArray(new String[values.size()]);

        return valuesArray[n];
    }

    @Override
    public String getHeaderFieldKey(int n) {
        initializeHeaders();

        Collection<String> keys = headers.keySet();
        String[] keysArray = keys.toArray(new String[keys.size()]);

        return keysArray[n];
    }

    @Override
    public int getContentLength() {
        initializeHeaders();

        return (int) length;
    }

    @Override
    public long getLastModified() {
        initializeHeaders();

        return lastModified;
    }

}

