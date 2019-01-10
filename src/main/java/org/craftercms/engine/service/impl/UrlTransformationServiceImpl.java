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
package org.craftercms.engine.service.impl;

import org.craftercms.core.service.CachingOptions;
import org.craftercms.engine.service.UrlTransformationService;
import org.craftercms.engine.service.context.SiteContext;

/**
 * Default implementation of {@link org.craftercms.engine.service.UrlTransformationService}.
 *
 * @author Alfonso Vásquez
 */
public class UrlTransformationServiceImpl implements UrlTransformationService {

    public String transform(String transformerName, String url) {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return siteContext.getUrlTransformationEngine().transformUrl(siteContext.getContext(), transformerName,
                                                                         url);
        } else {
            return url;
        }
    }

    public String transform(String transformerName, String url, boolean cache) {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            CachingOptions cachingOptions = new CachingOptions();
            cachingOptions.setDoCaching(cache);

            return siteContext.getUrlTransformationEngine().transformUrl(siteContext.getContext(), cachingOptions,
                                                                         transformerName, url);
        } else {
            return url;
        }
    }

}
