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
package org.craftercms.engine.util.freemarker;

import freemarker.template.*;
import org.craftercms.engine.service.context.SiteContext;

import static freemarker.template.utility.Collections12.singletonList;

/**
 * Implementation of {@link TemplateHashModelEx} to safely expose the {@link SiteContext} instance in templates
 *
 * @author joseross
 * @since 3.1.7
 */
public class SiteContextHashModel implements TemplateHashModelEx {

    public static final String SITE_NAME_KEY = "siteName";

    protected ObjectWrapper objectWrapper;

    public SiteContextHashModel(ObjectWrapper objectWrapper) {
        this.objectWrapper = objectWrapper;
    }

    @Override
    public int size() throws TemplateModelException {
        return 1;
    }

    @Override
    public TemplateCollectionModel keys() throws TemplateModelException {
        return new SimpleCollection(singletonList(SITE_NAME_KEY), objectWrapper);
    }

    @Override
    public TemplateCollectionModel values() throws TemplateModelException {
        return new SimpleCollection(singletonList(SiteContext.getCurrent().getSiteName()), objectWrapper);
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        if (SITE_NAME_KEY.equals(key)) {
            return objectWrapper.wrap(SiteContext.getCurrent().getSiteName());
        }
        return objectWrapper.wrap(null);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
