/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.engine.service;

import org.craftercms.core.exception.UrlTransformationException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.url.UrlTransformationEngine;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;

/**
 * Simple wrapper to a {@link UrlTransformationEngine} that uses the {@link UrlTransformationEngine} of the current context.
 *
 * @author Alfonso Vásquez
 */
public class UrlTransformationService {

    public String transform(String transformerName, String url) throws UrlTransformationException {
        SiteContext context = AbstractSiteContextResolvingFilter.getCurrentContext();

        return context.getUrlTransformationEngine().transformUrl(context.getContext(), transformerName, url);
    }

    public String transform(String transformerName, String url, boolean cache) throws UrlTransformationException {
        SiteContext context = AbstractSiteContextResolvingFilter.getCurrentContext();
        CachingOptions cachingOptions = new CachingOptions();

        cachingOptions.setDoCaching(cache);

        return context.getUrlTransformationEngine().transformUrl(context.getContext(), cachingOptions, transformerName, url);
    }

}
