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
package org.craftercms.engine.url;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.core.exception.UrlTransformationException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformer;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.craftercms.engine.util.TargetingUtils;
import org.craftercms.engine.properties.SiteProperties;
import org.springframework.beans.factory.annotation.Required;

/**
 * Transforms the URL into a targeted URL (if it's not a targeted URL yet).
 *
 * @author avasquez
 */
public class ToTargetedUrlTransformer implements UrlTransformer {

    protected TargetedUrlStrategy targetedUrlStrategy;
    protected boolean forceCurrentTargetId;

    @Required
    public void setTargetedUrlStrategy(TargetedUrlStrategy targetedUrlStrategy) {
        this.targetedUrlStrategy = targetedUrlStrategy;
    }

    public void setForceCurrentTargetId(boolean forceCurrentTargetId) {
        this.forceCurrentTargetId = forceCurrentTargetId;
    }

    @Override
    public String transformUrl(Context context, CachingOptions cachingOptions,
                               String url) throws UrlTransformationException {
        if (SiteProperties.isTargetingEnabled() && !TargetingUtils.excludePath(url)) {
            String rootFolder = TargetingUtils.getMatchingRootFolder(url);
            if (StringUtils.isNotEmpty(rootFolder)) {
                String relativeUrl = StringUtils.substringAfter(url, rootFolder);
                String targetedUrl = targetedUrlStrategy.toTargetedUrl(relativeUrl, forceCurrentTargetId);

                return UrlUtils.concat(rootFolder, targetedUrl);
            }
        }

        return url;
    }

}
