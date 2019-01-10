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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.Content;
import org.craftercms.engine.service.context.SiteContext;

/**
 * {@link java.net.URLStreamHandler} for handling {@link java.net.URLConnection}s to the Crafter content store.
 *
 * @author avasquez
 */
public class ContentStoreUrlStreamHandler extends URLStreamHandler {

    private static final String URL_REGEX = "[^:]+:.+";
    private static final String URL_SCHEME = "site";

    protected SiteContext siteContext;

    public ContentStoreUrlStreamHandler(SiteContext siteContext) {
        this.siteContext = siteContext;
    }

    public URL createUrl(String filename) throws MalformedURLException {
        if (!filename.matches(URL_REGEX)) {
            filename = URL_SCHEME + ':' + (!filename.startsWith("/")? "/" : "") + filename;
        }

        return new URL(null, filename, this);
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        try {
            Content content = siteContext.getStoreService().getContent(siteContext.getContext(), url.getFile());

            return new ContentStoreUrlConnection(url, content);
        } catch (PathNotFoundException e) {
            throw new FileNotFoundException("No content found at '" + url.getFile() + "' in content store");
        } catch (Exception e) {
            throw new IOException("Error retrieving content at '" + url.getFile() + "' in content store", e);
        }
    }

}
