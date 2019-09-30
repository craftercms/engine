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

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.profile.api.Profile;
import org.craftercms.search.elasticsearch.impl.AbstractElasticsearchWrapper;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.security.utils.SecurityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

/**
 * Implementation of {@link org.craftercms.search.elasticsearch.ElasticsearchWrapper}
 * that sets the index based on the current site context for all search requests.
 * @author joseross
 * @since 3.1
 */
public class SiteAwareElasticsearchService extends AbstractElasticsearchWrapper {

    private static final Logger logger = LoggerFactory.getLogger(SiteAwareElasticsearchService.class);

    private static final String DEFAULT_ROLE_FIELD_NAME = "authorizedRoles.item.role";

    /**
     * Format used to build the index id
     */
    protected String indexIdFormat;

    protected String roleFieldName = DEFAULT_ROLE_FIELD_NAME;

    @Required
    public void setIndexIdFormat(final String indexIdFormat) {
        this.indexIdFormat = indexIdFormat;
    }

    public void setRoleFieldName(final String roleFieldName) {
        this.roleFieldName = roleFieldName;
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

    @Override
    protected void updateFilters(final SearchRequest request) {
        super.updateFilters(request);

        BoolQueryBuilder mainQuery = (BoolQueryBuilder) request.source().query();

        Profile profile = SecurityUtils.getCurrentProfile();
        if (profile != null && CollectionUtils.isNotEmpty(profile.getRoles())) {
            logger.debug("Filtering search results for roles: {}", profile.getRoles());
            mainQuery.filter(boolQuery().must(boolQuery()
                .should(boolQuery().mustNot(existsQuery(roleFieldName)))
                .should(matchQuery(roleFieldName, String.join(" ", profile.getRoles())))
            ));
        } else {
            logger.debug("Filtering search to show only public items");
            mainQuery.filter(boolQuery().mustNot(existsQuery(roleFieldName)));
        }
    }

}
