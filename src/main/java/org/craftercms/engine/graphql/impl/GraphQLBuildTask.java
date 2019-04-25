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

package org.craftercms.engine.graphql.impl;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import graphql.GraphQL;
import org.craftercms.engine.graphql.GraphQLFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Utility class to perform a GraphQL schema build
 *
 * @author joseross
 * @since 3.1
 */
public class GraphQLBuildTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLBuildTask.class);

    private static final Lock lock = new ReentrantLock();

    /**
     * The {@link GraphQLFactory} instance
     */
    protected GraphQLFactory graphQLFactory;

    /**
     * The {@link SiteContext} to use
     */
    protected SiteContext siteContext;

    public GraphQLBuildTask(final SiteContext siteContext) {
        this.siteContext = siteContext;
    }

    @Required
    public void setGraphQLFactory(final GraphQLFactory graphQLFactory) {
        this.graphQLFactory = graphQLFactory;
    }

    public void setSiteContext(final SiteContext siteContext) {
        this.siteContext = siteContext;
    }

    @Override
    public void run() {
        if (lock.tryLock()) {
            logger.info("Starting GraphQL Schema build for site {}", siteContext.getSiteName());
            try {
                GraphQL graphQL = graphQLFactory.getInstance(siteContext);
                if (Objects.nonNull(graphQL)) {
                    siteContext.setGraphQL(graphQL);
                    logger.info("GraphQL Schema build completed for site {}", siteContext.getSiteName());
                }
            } catch (Exception e) {
                logger.error("Error building the GraphQL Schema for site {}", siteContext.getSiteName(), e);
            } finally {
                lock.unlock();
            }
        } else {
            logger.info("GraphQL Schema is already being built for site {}", siteContext.getSiteName());
        }
    }

}
