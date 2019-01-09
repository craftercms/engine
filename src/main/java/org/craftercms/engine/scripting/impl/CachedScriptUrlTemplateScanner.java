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

package org.craftercms.engine.scripting.impl;

import java.util.List;

import org.craftercms.commons.lang.Callback;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.event.SiteContextCreatedEvent;
import org.craftercms.engine.scripting.ScriptUrlTemplateScanner;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationListener;
import org.springframework.web.util.UriTemplate;

/**
 * {@link ScriptUrlTemplateScanner} decorator that caches the result of an actual scan, and also pre-caches
 * the URL templates on site context creation.
 *
 * @author avasquez
 */
public class CachedScriptUrlTemplateScanner implements ScriptUrlTemplateScanner,
    ApplicationListener<SiteContextCreatedEvent> {

    public static final String URL_TEMPLATES_CACHE_KEY_ELEM = "restScriptUrlTemplates";

    protected CacheTemplate cacheTemplate;
    protected ScriptUrlTemplateScanner actualScanner;

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Required
    public void setActualScanner(ScriptUrlTemplateScanner actualScanner) {
        this.actualScanner = actualScanner;
    }

    @Override
    public void onApplicationEvent(SiteContextCreatedEvent event) {
        // Pre-cache the url templates when a site context is created.
        scan(event.getSiteContext());
    }

    @Override
    public List<UriTemplate> scan(final SiteContext siteContext) {
        return cacheTemplate.getObject(siteContext.getContext(), new Callback<List<UriTemplate>>() {

            @Override
            public List<UriTemplate> execute() {
                return actualScanner.scan(siteContext);
            }

        }, URL_TEMPLATES_CACHE_KEY_ELEM);
    }

}
