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

import java.io.IOException;
import java.util.List;

import freemarker.cache.CacheStorage;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.craftercms.engine.macro.MacroResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

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

    private MacroResolver macroResolver;
    private TemplateExceptionHandler templateExceptionHandler;
    private CacheStorage cacheStorage;

    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    public void setTemplateExceptionHandler(TemplateExceptionHandler templateExceptionHandler) {
        this.templateExceptionHandler = templateExceptionHandler;
    }

    public void setCacheStorage(final CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
    }

    @Override
    protected void postProcessConfiguration(Configuration config) throws IOException, TemplateException {
        if (templateExceptionHandler != null) {
            config.setTemplateExceptionHandler(templateExceptionHandler);
        }
        if (cacheStorage != null) {
            config.setCacheStorage(cacheStorage);
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

}
