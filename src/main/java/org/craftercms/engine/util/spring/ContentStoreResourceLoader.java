/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.engine.util.spring;

import org.craftercms.engine.service.context.SiteContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * {@link org.springframework.core.io.ResourceLoader} that resolves paths a Crafter content store resource.
 *
 * @author avasquez
 */
public class ContentStoreResourceLoader extends DefaultResourceLoader {

    protected SiteContext siteContext;

    public ContentStoreResourceLoader(SiteContext siteContext) {
        this.siteContext = siteContext;
    }

    @Override
    protected Resource getResourceByPath(String path) {
        return new ContentStoreResource(siteContext, path);
    }

}
