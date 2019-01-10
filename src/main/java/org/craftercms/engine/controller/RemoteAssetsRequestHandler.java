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
package org.craftercms.engine.controller;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.file.stores.RemoteFile;
import org.craftercms.commons.file.stores.RemoteFileResolver;
import org.craftercms.engine.util.spring.resources.RangeAwareResourceRegionHttpMessageConverter;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Request handler to render static assets that are stored externally from the content store, in remote
 * file stores.
 *
 * @author @avasquez
 */
public class RemoteAssetsRequestHandler extends ResourceHttpRequestHandler {

    private RemoteFileResolver remoteFileResolver;
    private boolean disableCaching;

    protected void init() {
        if(disableCaching) {
            setCacheControl(CacheControl.noCache());
        }
        setRequireSession(false);
        setResourceRegionHttpMessageConverter(new RangeAwareResourceRegionHttpMessageConverter());
    }

    @Required
    public void setRemoteFileResolver(RemoteFileResolver remoteFileResolver) {
        this.remoteFileResolver = remoteFileResolver;
    }

    public void setDisableCaching(final boolean disableCaching) {
        this.disableCaching = disableCaching;
    }

    @Override
    protected Resource getResource(final HttpServletRequest request) throws IOException {
        String path = getPath(request);

        RemoteFile file = remoteFileResolver.resolve(path);
        if (file != null) {
            return file.toResource();
        } else {
            throw new FileNotFoundException("No remote file found for " + path);
        }
    }

    protected String getPath(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (StringUtils.isNotEmpty(path)) {
            return !path.startsWith("/") ? "/" + path : path;
        } else {
            throw new IllegalStateException("Required request attribute '" +
                                            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
        }
    }

}
