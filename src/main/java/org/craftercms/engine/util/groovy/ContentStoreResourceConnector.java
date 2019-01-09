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

package org.craftercms.engine.util.groovy;

import java.net.URLConnection;

import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.url.ContentStoreUrlStreamHandler;

/**
 * Implementation of {@link groovy.util.ResourceConnector} for retrieving {@link java.net.URLConnection} to
 * content in a Crafter content store.
 *
 * @author Alfonso Vásquez
 */
public class ContentStoreResourceConnector implements ResourceConnector {

    protected ContentStoreUrlStreamHandler urlStreamHandler;

    public ContentStoreResourceConnector(SiteContext siteContext) {
        urlStreamHandler = new ContentStoreUrlStreamHandler(siteContext);
    }

    @Override
    public URLConnection getResourceConnection(String name) throws ResourceException {
        try {
            return urlStreamHandler.createUrl(name).openConnection();
        } catch (Exception e) {
            throw new ResourceException("Unable to open URL connection to '" + name + "'", e);
        }
    }

}
