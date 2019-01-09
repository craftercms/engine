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
import java.io.InputStreamReader;
import java.io.Reader;

import freemarker.cache.TemplateLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;

/**
 * Freemarker {@link freemarker.cache.TemplateLoader} similar to {@link org.springframework.ui.freemarker.SpringTemplateLoader} but instead of using
 * Spring Resources, it uses the {@link ContentStoreService#getContent(Context, String)}.
 *
 * @author Alfonso Vásquez
 */
public class CrafterFreeMarkerTemplateLoader implements TemplateLoader {

    private static final Log logger = LogFactory.getLog(CrafterFreeMarkerTemplateLoader.class);

    private ContentStoreService contentStoreService;

    @Required
    public void setContentStoreService(ContentStoreService contentStoreService) {
        this.contentStoreService = contentStoreService;
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
        return UrlUtils.concat(siteContext.getTemplatesPath(), name);
    }

}
