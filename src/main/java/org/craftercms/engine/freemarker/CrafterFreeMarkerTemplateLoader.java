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
package org.craftercms.engine.freemarker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import freemarker.cache.TemplateLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.service.context.SiteContext;

import static org.craftercms.commons.lang.RegexUtils.matchesAny;

/**
 * Freemarker {@link freemarker.cache.TemplateLoader} similar to {@link org.springframework.ui.freemarker.SpringTemplateLoader} but instead of using
 * Spring Resources, it uses the {@link ContentStoreService#getContent(Context, String)}.
 *
 * @author Alfonso Vásquez
 */
public class CrafterFreeMarkerTemplateLoader implements TemplateLoader {

    private static final Log logger = LogFactory.getLog(CrafterFreeMarkerTemplateLoader.class);

    private ContentStoreService contentStoreService;

    private String[] globalAllowedPaths;

    public CrafterFreeMarkerTemplateLoader(ContentStoreService contentStoreService, String[] globalAllowedPaths) {
        this.contentStoreService = contentStoreService;
        this.globalAllowedPaths = globalAllowedPaths;
    }

    @Override
    public Object findTemplateSource(String name) throws IOException {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            String path = getTemplatePath(siteContext, name);

            if (logger.isDebugEnabled()) {
                logger.debug("Looking for FreeMarker template at [context=" + siteContext + ", path='" + path + "']");
            }

            Content content = contentStoreService.findContent(siteContext.getContext(), path);
            if (content == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to find FreeMarker template at [context=" + siteContext + ", path='" + path +
                                 "']");
                }
            }

            return content;
        } else {
            return null;
        }
    }

    @Override
    public long getLastModified(Object templateSource) {
        return ((Content) templateSource).getLastModified();
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        return new InputStreamReader(((Content) templateSource).getInputStream(), encoding);
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
    }

    protected String getTemplatePath(SiteContext siteContext, String name) {
        String templatesPath = siteContext.getTemplatesPath();
        if (StringUtils.startsWith(name, StringUtils.removeStart(templatesPath, "/")) ||
                matchesAny(name, globalAllowedPaths) ||
                matchesAny(name, siteContext.getAllowedTemplatePaths())) {
            return name;
        } else {
            return UrlUtils.concat(templatesPath, name);
        }
    }

}
