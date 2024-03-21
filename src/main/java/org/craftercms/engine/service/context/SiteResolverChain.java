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
package org.craftercms.engine.service.context;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that goes through a chain of resolvers until one of
 * them returns a non-empty site.
 *
 * @author avasquez
 */
public class SiteResolverChain implements SiteResolver {

    protected List<SiteResolver> chain;

    public SiteResolverChain(List<SiteResolver> chain) {
        this.chain = chain;
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        for (SiteResolver resolver : chain) {
            String site = resolver.getSiteName(request);
            if (StringUtils.isNotEmpty(site)) {
                return site;
            }
        }

        return null;
    }

}
