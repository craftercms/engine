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
package org.craftercms.engine.view;

import org.craftercms.engine.service.context.SiteContext;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

/**
 * {@code ViewResolver} decorator that caches views on Crafter's own cache per site.
 *
 * <p>
 *     <strong>NOTE:</strong> if you're decorating a  {@code AbstractCachingViewResolver} please make sure you turn
 *     off the caching of that view resolver.
 * </p>
 *
 * @author avasquez
 * @since 3.1.5
 */
public class CrafterCacheAwareViewResolverDecorator implements ViewResolver, Ordered {

    private static final String VIEW_CONST_KEY_ELEM = "view";

    protected int order;
    protected ViewResolver actualViewResolver;

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setActualViewResolver(ViewResolver actualViewResolver) {
        this.actualViewResolver = actualViewResolver;
    }

    @Override
    public View resolveViewName(@NonNull String viewName, @NonNull Locale locale) throws Exception {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            try {
                return siteContext.getCacheTemplate().getObject(siteContext.getContext(), () -> {
                    try {
                        return actualViewResolver.resolveViewName(viewName, locale);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, viewName, locale, VIEW_CONST_KEY_ELEM);
            } catch (RuntimeException e) {
                throw (Exception) e.getCause();
            }
        } else {
            return actualViewResolver.resolveViewName(viewName, locale);
        }
    }

}
