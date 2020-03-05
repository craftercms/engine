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
package org.craftercms.engine.util.blob;

import org.craftercms.commons.file.blob.EnvironmentResolver;
import org.craftercms.engine.service.context.SiteContext;

/**
 * Implementation of {@link EnvironmentResolver} that uses the current {@link SiteContext}
 *
 * @author joseross
 * @since 3.1.6
 */
public class SiteAwareEnvironmentResolver implements EnvironmentResolver {

    protected boolean isPreview;

    protected String stagingPattern;

    public SiteAwareEnvironmentResolver(boolean isPreview, String stagingPattern) {
        this.isPreview = isPreview;
        this.stagingPattern = stagingPattern;
    }

    @Override
    public String getEnvironment() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext == null) {
            throw new IllegalStateException("Can't resolve the current site environment");
        }
        if (isPreview) {
            return PREVIEW;
        } else if(siteContext.getSiteName().matches(stagingPattern)) {
            return STAGING;
        } else {
            return LIVE;
        }
    }

}
