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
package org.craftercms.engine.util.config;

import org.craftercms.commons.config.PublishingTargetResolver;
import org.craftercms.engine.service.context.SiteContext;

import java.beans.ConstructorProperties;

/**
 * Implementation of {@link PublishingTargetResolver} that uses the current {@link SiteContext}
 *
 * @author joseross
 * @since 3.1.6
 */
public class SiteAwarePublishingTargetResolver implements PublishingTargetResolver {

    protected boolean preview;

    protected String stagingPattern;

    @ConstructorProperties({"preview", "stagingPattern"})
    public SiteAwarePublishingTargetResolver(boolean preview, String stagingPattern) {
        this.preview = preview;
        this.stagingPattern = stagingPattern;
    }

    @Override
    public String getPublishingTarget() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            throw new IllegalStateException("Can't resolve the current site target");
        }
        return getPublishingTarget(siteContext.getSiteName());
    }

    public String getPublishingTarget(String siteName) {
        if (preview) {
            return PREVIEW;
        } else if(siteName.matches(stagingPattern)) {
            return STAGING;
        } else {
            return LIVE;
        }
    }

}
