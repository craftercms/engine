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
package org.craftercms.engine.freemarker;

import freemarker.cache.CacheStorage;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.*;
import org.craftercms.core.exception.CrafterException;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.macro.MacroResolver;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Extends {@link org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer} to:
 *
 * <ul>
 *     <li>Macro-resolve the template loader paths before a template loader is created for the path</li>
 * </ul>
 *
 * @author Alfonso Vásquez
 */
public class CrafterFreeMarkerConfigurer extends FreeMarkerConfigurer {

    public static final String CACHE_CONST_KEY_ELEM_TEMPLATE = "freemarkerTemplate";

    private MacroResolver macroResolver;
    private TemplateExceptionHandler templateExceptionHandler;
    private CacheStorage freemarkerCacheStorage;
    private CacheTemplate cacheTemplate;

    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    public void setTemplateExceptionHandler(TemplateExceptionHandler templateExceptionHandler) {
        this.templateExceptionHandler = templateExceptionHandler;
    }

    public void setFreemarkerCacheStorage(final CacheStorage freemarkerCacheStorage) {
        this.freemarkerCacheStorage = freemarkerCacheStorage;
    }

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Override
    protected Configuration newConfiguration() throws IOException, TemplateException {
        return new CrafterCacheAwareConfiguration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    }

    @Override
    protected void postProcessConfiguration(Configuration config) throws IOException, TemplateException {
        if (templateExceptionHandler != null) {
            config.setTemplateExceptionHandler(templateExceptionHandler);
        }
        if (freemarkerCacheStorage != null) {
            config.setCacheStorage(freemarkerCacheStorage);
        }
    }

    @Override
    protected TemplateLoader getTemplateLoaderForPath(String templateLoaderPath) {
        if (macroResolver != null) {
            templateLoaderPath = macroResolver.resolveMacros(templateLoaderPath);
        }

        return super.getTemplateLoaderForPath(templateLoaderPath);
    }

    @Override
    protected void postProcessTemplateLoaders(List<TemplateLoader> templateLoaders) {
        // Overwrote to get rid of the log.info
        templateLoaders.add(new ClassTemplateLoader(FreeMarkerConfigurer.class, ""));
    }

    /**
     * Freemarker {@code Configuration} class extension that caches the template in the Crafter Cache, allowing for
     * templates to be loaded and parsed once, even when concurrent threads are trying to retrieve the same template.
     */
    public class CrafterCacheAwareConfiguration extends Configuration {

        public CrafterCacheAwareConfiguration(Version incompatibleImprovements) {
            super(incompatibleImprovements);
        }

        @Override
        public Template getTemplate(String name, Locale locale, Object customLookupCondition, String encoding,
                                    boolean parseAsFTL, boolean ignoreMissing) throws IOException {
            SiteContext siteContext = SiteContext.getCurrent();
            if (siteContext != null) {
                try {
                    return cacheTemplate.getObject(siteContext.getContext(), () -> {
                        try {
                            return super.getTemplate(name, locale, customLookupCondition, encoding, parseAsFTL,
                                                     ignoreMissing);
                        } catch (Exception e) {
                            throw new CrafterException(e);
                        }
                    }, name, locale, customLookupCondition, encoding, parseAsFTL, CACHE_CONST_KEY_ELEM_TEMPLATE);
                } catch (CrafterException e) {
                    throw (IOException) e.getCause();
                }
            } else {
                return super.getTemplate(name, locale, customLookupCondition, encoding, parseAsFTL, ignoreMissing);
            }
        }
    }

}
