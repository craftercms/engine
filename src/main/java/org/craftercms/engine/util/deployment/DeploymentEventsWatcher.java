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
package org.craftercms.engine.util.deployment;

import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.event.*;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class that runs on a cron job (configurable) and checks every site to see if they have a deployment
 * events file (by default {@code deployment-events.properties}, which should contain timestamps sent by the
 * Deployer indicating requests for clearing the site cache and/or rebuilding the context.
 *
 * @author avasquez
 */
public class DeploymentEventsWatcher implements ApplicationListener<ApplicationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentEventsWatcher.class);

    public static final String DEFAULT_DEPLOYMENT_EVENTS_FILE_URL = "deployment-events.properties";

    private static final String LATEST_EVENT_KEY_FORMAT = "siteName=%s, eventType=%s";

    private static final String CLEAR_CACHE_EVENT_KEY = "events.deployment.clearCache";
    private static final String REBUILD_CONTEXT_EVENT_KEY = "events.deployment.rebuildContext";
    private static final String REBUILD_GRAPHQL_EVENT_KEY = "events.deployment.rebuildGraphQL";

    private String deploymentEventsFileUrl;
    private SiteContextManager siteContextManager;

    private volatile boolean startupCompleted;
    private Map<String, SiteEvent> latestEvents;

    public DeploymentEventsWatcher() {
        this.deploymentEventsFileUrl = DEFAULT_DEPLOYMENT_EVENTS_FILE_URL;
        this.startupCompleted = false;
        this.latestEvents = new ConcurrentHashMap<>();
    }

    public void setDeploymentEventsFileUrl(String deploymentEventsFileUrl) {
        this.deploymentEventsFileUrl = deploymentEventsFileUrl;
    }

    @Required
    public void setSiteContextManager(SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    public void checkForEvents() {
        if (startupCompleted) {
            logger.debug("Deployment events watcher running...");

            siteContextManager.syncContexts();

            for (SiteContext siteContext : siteContextManager.listContexts()) {
                checkForSiteEvents(siteContext);
            }
        }
    }

    public void checkForSiteEvents(SiteContext siteContext) {
        String siteName = siteContext.getSiteName();
        Properties deploymentEvents;

        try {
            deploymentEvents = loadDeploymentEvents(siteContext);
        } catch (IOException e) {
            logger.error("Unable to load deployment events for site '{}'", siteName, e);
            return;
        }

        boolean rebuildContextTriggered = false;

        logger.debug("Checking deployment events for site {}...", siteName);

        long lastContextBuildEvent = getLatestEventTimestamp(siteName, SiteContextCreatedEvent.class);
        
        if (deploymentEvents.containsKey(REBUILD_CONTEXT_EVENT_KEY)) {
            long rebuildContextEvent = getEventProperty(deploymentEvents, REBUILD_CONTEXT_EVENT_KEY);
            
            if (lastContextBuildEvent < rebuildContextEvent) {
                logger.info("Rebuild context deployment event received. Rebuilding context for site {}...", siteName);

                siteContextManager.startContextRebuild(siteContext.getSiteName(), siteContext.isFallback());

                rebuildContextTriggered = true;
            }
        }

        if (!rebuildContextTriggered && deploymentEvents.containsKey(CLEAR_CACHE_EVENT_KEY)) {
            long clearCacheEvent = getEventProperty(deploymentEvents, CLEAR_CACHE_EVENT_KEY);
            long lastCacheClearEvent = getLatestEventTimestamp(siteName, CacheClearStartedEvent.class);

            if (lastContextBuildEvent < clearCacheEvent && lastCacheClearEvent < clearCacheEvent) {
                logger.info("Clear cache deployment event received. Clearing cache for site {}...", siteName);

                siteContext.startCacheClear();
            }
        }

        if(!rebuildContextTriggered && deploymentEvents.containsKey(REBUILD_GRAPHQL_EVENT_KEY)) {
            long rebuildGraphQLEvent = getEventProperty(deploymentEvents, REBUILD_GRAPHQL_EVENT_KEY);
            long lastRebuildGraphQLEvent = getLatestEventTimestamp(siteName, GraphQLBuildStartedEvent.class);

            if (lastContextBuildEvent < rebuildGraphQLEvent && lastRebuildGraphQLEvent < rebuildGraphQLEvent) {
                logger.info("Rebuild GraphQL deployment event received. Rebuilding schema for site {}...", siteName);

                siteContext.startGraphQLSchemaBuild();
            }
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof SiteContextInitializedEvent) {
            startupCompleted = true;
        } else if (event instanceof SiteEvent) {
            SiteEvent siteEvent = (SiteEvent) event;
            String siteName = siteEvent.getSiteContext().getSiteName();
            Class<? extends SiteEvent> eventClass = siteEvent.getClass();

            if (eventClass.equals(SiteContextCreatedEvent.class) ||
                eventClass.equals(CacheClearStartedEvent.class) ||
                eventClass.equals(GraphQLBuildStartedEvent.class)) {
                latestEvents.put(String.format(LATEST_EVENT_KEY_FORMAT, siteName, eventClass), siteEvent);
            }
        }
    }

    private long getLatestEventTimestamp(String siteName, Class<? extends SiteEvent> eventClass) {
        SiteEvent event = latestEvents.get(String.format(LATEST_EVENT_KEY_FORMAT, siteName, eventClass));
        if (event != null) {
            return event.getTimestamp();
        } else {
            return -1;
        }
    }

    private Properties loadDeploymentEvents(SiteContext siteContext) throws IOException {
        ContentStoreService contentStoreService = siteContext.getStoreService();
        Context context = siteContext.getContext();
        CachingOptions cachingOptions = CachingOptions.CACHE_OFF_CACHING_OPTIONS;
        Content content = contentStoreService.findContent(context, cachingOptions, deploymentEventsFileUrl);
        Properties events = new Properties();

        if (content != null) {
            events.load(new InputStreamReader(content.getInputStream(), StandardCharsets.UTF_8));
        }

        return events;
    }

    private long getEventProperty(Properties deploymentEvents, String name) {
        return Instant.parse(deploymentEvents.getProperty(name)).toEpochMilli();
    }

}
