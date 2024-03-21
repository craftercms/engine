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

package org.craftercms.engine.scripting.impl;

import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.event.SiteContextInitializedEvent;
import org.craftercms.engine.scripting.ScriptUrlTemplateScanner;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.context.ApplicationListener;
import org.springframework.web.util.UriTemplate;

import java.util.List;

/**
 * {@link ScriptUrlTemplateScanner} decorator that caches the result of an actual scan, and also pre-caches
 * the URL templates on site context creation.
 *
 * @author avasquez
 */
public class CachedScriptUrlTemplateScanner implements ScriptUrlTemplateScanner,
                                                       ApplicationListener<SiteContextInitializedEvent> {

    public static final String URL_TEMPLATES_CACHE_KEY_ELEM = "restScriptUrlTemplates";

    protected CacheTemplate cacheTemplate;
    protected ScriptUrlTemplateScanner actualScanner;

    public CachedScriptUrlTemplateScanner(CacheTemplate cacheTemplate, ScriptUrlTemplateScanner actualScanner) {
        this.cacheTemplate = cacheTemplate;
        this.actualScanner = actualScanner;
    }

    @Override
    public void onApplicationEvent(SiteContextInitializedEvent event) {
        // Pre-cache the url templates after a site context is initialized.
        scan(event.getSiteContext());
    }

    @Override
    public List<UriTemplate> scan(final SiteContext siteContext) {
        return cacheTemplate.getObject(
                siteContext.getContext(), () -> actualScanner.scan(siteContext), URL_TEMPLATES_CACHE_KEY_ELEM);
    }

}
