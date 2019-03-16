package org.craftercms.engine.util.deployment;

import org.craftercms.core.service.*;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class that runs on a cron job (configurable) and checks every site to see if they have a deployment
 * events file (by default {@code deployment-events.properties}, which should contain timestamps sent by the
 * Deployer indicating requests for clearing the site cache and/or rebuilding the context.
 *
 * @author avasquez
 */
public class DeploymentEventsWatcher {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentEventsWatcher.class);

    public static final String DEFAULT_DEPLOYMENT_EVENTS_FILE_URL = "deployment-events.properties";

    private static final String CLEAR_CACHE_EVENT_KEY = "events.deployment.clearCache";
    private static final String REBUILD_CONTEXT_EVENT_KEY = "events.deployment.rebuildContext";

    private String deploymentEventsFileUrl;
    private CacheService cacheService;
    private SiteContextManager siteContextManager;

    public DeploymentEventsWatcher() {
        this.deploymentEventsFileUrl = DEFAULT_DEPLOYMENT_EVENTS_FILE_URL;
    }

    public void setDeploymentEventsFileUrl(String deploymentEventsFileUrl) {
        this.deploymentEventsFileUrl = deploymentEventsFileUrl;
    }

    @Required
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Required
    public void setSiteContextManager(SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    public void checkForEvents() {
        logger.debug("Deployment events watcher running...");

        for (SiteContext siteContext : siteContextManager.listContexts()) {
            try {
                checkForSiteEvents(siteContext);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkForSiteEvents(SiteContext siteContext) throws IOException {
        Properties deploymentEvents = loadDeploymentEvents(siteContext);
        Map<String, Instant> siteEvents = siteContext.getEvents();
        String siteName = siteContext.getSiteName();
        boolean rebuildContextTriggered = false;

        logger.debug("Checking deployment events for site {}...", siteName);
        
        if (deploymentEvents.containsKey(REBUILD_CONTEXT_EVENT_KEY)) {
            Instant rebuildContextEvent = Instant.parse(deploymentEvents.getProperty(REBUILD_CONTEXT_EVENT_KEY));
            Instant lastContextBuildEvent = siteEvents.get(SiteContext.CONTEXT_BUILT_EVENT_KEY);
            
            if (lastContextBuildEvent.isBefore(rebuildContextEvent)) {
                logger.info("Rebuild context deployment event received. Rebuilding context for site {}...", siteName);

                siteContextManager.rebuildContext(siteContext.getSiteName(), siteContext.isFallback());

                rebuildContextTriggered = true;
            }
        }

        if (!rebuildContextTriggered && deploymentEvents.containsKey(CLEAR_CACHE_EVENT_KEY)) {
            Instant clearCacheEvent = Instant.parse(deploymentEvents.getProperty(CLEAR_CACHE_EVENT_KEY));
            Instant lastCacheClearEvent = siteEvents.get(SiteContext.CACHE_CLEARED_EVENT_KEY);

            if (lastCacheClearEvent.isBefore(clearCacheEvent)) {
                logger.info("Clear cache deployment event received. Clearing cache for site {}...", siteName);

                siteContext.clearCache(cacheService);
            }
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

}
