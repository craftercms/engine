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

import java.net.MalformedURLException;
import java.net.URL;

import groovy.lang.GroovyResourceLoader;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.engine.scripting.impl.GroovyScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.url.ContentStoreUrlStreamHandler;

/**
 * Implementation of {@link org.craftercms.engine.util.groovy.ContentStoreGroovyResourceLoader} that uses Crafter
 * Core's content store to load Groovy resources.
 *
 * @author avasquez
 */
public class ContentStoreGroovyResourceLoader implements GroovyResourceLoader {

    protected SiteContext siteContext;
    protected ContentStoreUrlStreamHandler urlStreamHandler;
    protected String groovyResourcesUrlPrefix;

    public ContentStoreGroovyResourceLoader(SiteContext siteContext, String groovyResourcesUrlPrefix) {
        this.siteContext = siteContext;
        this.urlStreamHandler = new ContentStoreUrlStreamHandler(siteContext);
        this.groovyResourcesUrlPrefix = groovyResourcesUrlPrefix;
    }

    @Override
    public URL loadGroovySource(String filename) throws MalformedURLException {
        if (filename.contains(".")) {
            filename = filename.replace('.', '/');
        }
        if (!filename.endsWith(GroovyScriptFactory.GROOVY_FILE_EXTENSION)) {
            filename += "." + GroovyScriptFactory.GROOVY_FILE_EXTENSION;
        }
        if (StringUtils.isNotEmpty(groovyResourcesUrlPrefix)) {
            filename = UrlUtils.concat(groovyResourcesUrlPrefix, filename);
        }

        if (siteContext.getStoreService().exists(siteContext.getContext(), filename)){
            return urlStreamHandler.createUrl(filename);
        } else {
            return null;
        }
    }

}
