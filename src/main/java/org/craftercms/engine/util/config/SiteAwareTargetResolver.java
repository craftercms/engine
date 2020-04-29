/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.commons.config.TargetResolver;
import org.craftercms.engine.service.context.SiteContext;

/**
 * Implementation of {@link TargetResolver} that uses the current {@link SiteContext}
 *
 * @author joseross
 * @since 3.1.6
 */
public class SiteAwareTargetResolver implements TargetResolver {

    protected boolean preview;

    protected String stagingPattern;

    public SiteAwareTargetResolver(boolean preview, String stagingPattern) {
        this.preview = preview;
        this.stagingPattern = stagingPattern;
    }

    @Override
    public String getTarget() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            throw new IllegalStateException("Can't resolve the current site target");
        }
        return getTarget(siteContext.getSiteName());
    }

    public String getTarget(String siteName) {
        if (preview) {
            return PREVIEW;
        } else if(siteName.matches(stagingPattern)) {
            return STAGING;
        } else {
            return LIVE;
        }
    }

}
