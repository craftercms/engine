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

package org.craftercms.engine.search;

import org.craftercms.search.elasticsearch.impl.AbstractElasticsearchWrapper;
import org.craftercms.engine.service.context.SiteContext;
import org.elasticsearch.action.search.SearchRequest;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link org.craftercms.search.elasticsearch.ElasticsearchWrapper}
 * that sets the index based on the current site context for all search requests.
 * @author joseross
 * @since 3.1
 */
public class SiteAwareElasticsearchService extends AbstractElasticsearchWrapper {

    /**
     * Format used to build the index id
     */
    protected String indexIdFormat;

    @Required
    public void setIndexIdFormat(final String indexIdFormat) {
        this.indexIdFormat = indexIdFormat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateIndex(final SearchRequest request) {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            request.indices(String.format(indexIdFormat, siteContext.getSiteName()));
        } else {
            throw new IllegalStateException("Current site context not found");
        }
    }

}
