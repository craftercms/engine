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

import io.methvin.watcher.DirectoryWatcher;
import io.methvin.watcher.hashing.FileHasher;
import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.commons.concurrent.locks.KeyBasedLockFactory;
import org.craftercms.commons.concurrent.locks.WeakKeyBasedReentrantLockFactory;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.engine.event.SiteContextPurgedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Registry and lifecycle manager of {@link SiteContext}s.
 *
 * @author Alfonso Vásquez
 */
@Validated
public class SiteContextManager implements ApplicationContextAware, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(SiteContextManager.class);

    protected ApplicationContext applicationContext;
    protected KeyBasedLockFactory<ReentrantLock> siteLockFactory;
    protected Map<String, SiteContext> contextRegistry;
    protected SiteContextFactory contextFactory;
    protected SiteContextFactory fallbackContextFactory;
    protected SiteListResolver siteListResolver;
    protected EntitlementValidator entitlementValidator;
    protected boolean waitForContextInit;
    protected Executor jobThreadPoolExecutor;
    protected String defaultSiteName;

    /**
     * Directory watcher registry for each site
     */
    protected Map<String, DirectoryWatcher> directoryWatcherRegistry;

    /**
     * Directory watcher processed event hash
     */
    protected Map<String, String> directoryWatcherLastProcessedHash;

    /**
     * Directory watcher counter to count modified events
     */
    protected Map<String, AtomicInteger> directoryWatcherCounter;

    /**
     * Directory watcher site rebuild tasks
     */
    protected Map<String, ScheduledExecutorService> directoryWatcherExecutor;

    /**
     * Directory watcher watch paths
     */
    protected String[] watcherPaths = {};

    /**
     * Directory watcher ignore paths
     */
    protected String[] watcherIgnorePaths = {};

    /**
     * Directory watcher counter limit to run rebuild
     */
    protected int watcherCounterLimit;

    /**
     * Directory watcher interval period to run rebuild task check
     */
    protected int watcherIntervalPeriod;

    /**
     * Context build retry max count
     */
    protected int contextBuildRetryMaxCount;

    /**
     * Context build retry wait time base in milliseconds
     */
    protected long contextBuildRetryWaitTimeBase;

    /**
     * Context build retry wait time multiple
     */
    protected int contextBuildRetryWaitTimeMultiplier;

    /**
     * true if Engine is in preview mode, false otherwise
     */
    protected boolean modePreview;

    public SiteContextManager() {
        siteLockFactory = new WeakKeyBasedReentrantLockFactory();
        contextRegistry = new ConcurrentHashMap<>();
        directoryWatcherRegistry = new ConcurrentHashMap<>();
        directoryWatcherLastProcessedHash = new HashMap<>();
        directoryWatcherCounter = new ConcurrentHashMap<>();
        directoryWatcherExecutor = new ConcurrentHashMap<>();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Required
    public void setContextFactory(SiteContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Required
    public void setFallbackContextFactory(SiteContextFactory fallbackContextFactory) {
        this.fallbackContextFactory = fallbackContextFactory;
    }

    @Required
    public void setSiteListResolver(final SiteListResolver siteListResolver) {
        this.siteListResolver = siteListResolver;
    }

    @Autowired
    public void setEntitlementValidator(@Lazy final EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    @Required
    public void setWaitForContextInit(boolean waitForContextInit) {
        this.waitForContextInit = waitForContextInit;
    }

    @Required
    public void setJobThreadPoolExecutor(Executor jobThreadPoolExecutor) {
        this.jobThreadPoolExecutor = jobThreadPoolExecutor;
    }

    @Required
    public void setDefaultSiteName(final String defaultSiteName) {
        this.defaultSiteName = defaultSiteName;
    }

    @Required
    public void setContextBuildRetryMaxCount(final int contextBuildRetryMaxCount) {
        this.contextBuildRetryMaxCount = contextBuildRetryMaxCount;
    }

    @Required
    public void setContextBuildRetryWaitTimeBase(final long contextBuildRetryWaitTimeBase) {
        this.contextBuildRetryWaitTimeBase = contextBuildRetryWaitTimeBase;
    }

    @Required
    public void setContextBuildRetryWaitTimeMultiplier(final int contextBuildRetryWaitTimeMultiplier) {
        this.contextBuildRetryWaitTimeMultiplier = contextBuildRetryWaitTimeMultiplier;
    }

    @Required
    public void setModePreview(final boolean modePreview) {
        this.modePreview = modePreview;
    }

    @Required
    public void setWatcherPaths(final String[] watcherPaths) {
        this.watcherPaths = watcherPaths;
    }

    @Required
    public void setWatcherIgnorePaths(final String[] watcherIgnorePaths) {
        this.watcherIgnorePaths = watcherIgnorePaths;
    }

    @Required
    public void setWatcherCounterLimit(final int watcherCounterLimit) {
        this.watcherCounterLimit = watcherCounterLimit;
    }

    @Required
    public void setWatcherIntervalPeriod(final int watcherIntervalPeriod) {
        this.watcherIntervalPeriod = watcherIntervalPeriod;
    }

    public void destroy() {
        destroyAllContexts();
    }

    public Collection<SiteContext> listContexts() {
        return contextRegistry.values();
    }

    /**
     * Creates all contexts (if not already created) from the site list resolver
     *
     * @param concurrent if the context creation should be done concurrently
     */
    public void createContexts(boolean concurrent) {
        Collection<String> siteNames = siteListResolver.getSiteList();

        logger.info("==================================================");
        logger.info("<CREATING SITE CONTEXTS>");
        logger.info("==================================================");

        if (CollectionUtils.isNotEmpty(siteNames)) {
            if (concurrent) {
                CompletionService<SiteContext> cs = new ExecutorCompletionService<>(jobThreadPoolExecutor);
                for (String siteName : siteNames) {
                    cs.submit(() -> getContextWithRetries(siteName));
                }

                for (int i = 0; i < siteNames.size(); i++) {
                    try {
                        cs.take();
                    } catch (InterruptedException e) {
                        logger.error("Stopping creation of site contexts, thread interrupted", e);
                        return;
                    }
                }
            } else {
                for (String siteName : siteNames) {
                    try {
                        getContextWithRetries(siteName);
                    } catch (InterruptedException e) {
                        logger.error("Stopping creation of site contexts, thread interrupted", e);
                        return;
                    }

                }
            }
        }

        logger.info("==================================================");
        logger.info("</CREATING SITE CONTEXTS>");
        logger.info("==================================================");
    }

    /**
     * Register files watcher for preview mode
     * Any files from watcherPaths will be watched for CREATE, MODIFY, DELETE actions
     * @param siteName site name
     */
    protected void registerPreviewWatcher(String siteName) {
        try {
            String siteRootPath = contextFactory.resolveRootFolderPath(siteName);
            List<Path> paths = Arrays.stream(watcherPaths)
                    .map(resource -> Paths.get(siteRootPath + resource))
                    .collect(Collectors.toList());
            List<Path> ignorePaths = Arrays.stream(watcherIgnorePaths)
                    .map(resource -> Paths.get(siteRootPath + resource))
                    .collect(Collectors.toList());
            DirectoryWatcher watcher = DirectoryWatcher.builder()
                    .paths(paths)
                    .fileHasher(FileHasher.LAST_MODIFIED_TIME)
                    .listener(event -> {
                        switch (event.eventType()) {
                            case CREATE:
                            case DELETE:
                            case MODIFY: {
                                boolean pathIgnored = ignorePaths.stream().anyMatch(ignorePath -> event.path().startsWith(ignorePath));
                                if (pathIgnored) {
                                    return;
                                }

                                String hashValue = event.hash() != null ? event.hash().asString() : "none";
                                logger.info("File watcher event type: '{}'. File affected: '{}'. Hash value: '{}'", event.eventType(), event.path(), hashValue);
                                // Only process if the hash is different from the last processed hash value,
                                // or the event hash is null in the case of DELETE
                                // This means the modified time has been updated from last processed event or the file is deleted
                                // This prevents multiple events for batch files change with same modified date such as from a git pull
                                String lastProcessedHash = directoryWatcherLastProcessedHash.get(siteName);
                                if (lastProcessedHash == null || event.hash() == null || !lastProcessedHash.equals(hashValue)) {
                                    Lock siteLock = siteLockFactory.getLock(siteName);
                                    siteLock.lock();
                                    try {
                                        if (event.hash() != null) {
                                            directoryWatcherLastProcessedHash.put(siteName, hashValue);
                                        }
                                        AtomicInteger counter = directoryWatcherCounter.get(siteName);
                                        if (counter == null) {
                                            counter = new AtomicInteger(0);
                                        }
                                        counter.getAndIncrement();
                                        directoryWatcherCounter.put(siteName, counter);
                                    } finally {
                                        siteLock.unlock();
                                    }
                                } else {
                                    logger.debug("File watch for hash '{}' has already processed. No action required.", hashValue);
                                }
                                break;
                            }
                            default:
                                logger.debug("File watcher unhandled event type: '{}'. File affected: '{}'.", event.eventType(), event.path());
                        }
                    })
                    .build();

            // Remove old watcher before register a new one
            if (directoryWatcherRegistry.get(siteName) != null) {
                Lock siteLock = siteLockFactory.getLock(siteName);
                siteLock.lock();
                try {
                    DirectoryWatcher oldWatcher = directoryWatcherRegistry.remove(siteName);
                    oldWatcher.close();
                } finally {
                    siteLock.unlock();
                }
            }
            directoryWatcherRegistry.put(siteName, watcher);
            watcher.watchAsync();
        } catch (Exception e) {
            logger.error("Error while creating watcher for site: '{}'", siteName, e);
        }
    }

    /**
     * Register preview rebuild task.
     * When the watcher triggers, set a counter to 1 and sleep for 200 (configurable) milliseconds.
     * Wake up and check:
     * Is the counter 5 (one second has passed)? Then, trigger a rebuild.
     * Has anything else changed (more changes since we slept)? If so, increment the counter and sleep for 200 milliseconds.
     * If nothing has changed, then trigger a rebuild.
     * @param siteName site name
     * @param isFallback is fallback
     */
    public void registerPreviewRebuildTask(String siteName, boolean isFallback) {
        // Remove old executor then register a new one
        if (directoryWatcherExecutor.get(siteName) != null) {
            Lock siteLock = siteLockFactory.getLock(siteName);
            siteLock.lock();
            try {
                ScheduledExecutorService oldExecutor = directoryWatcherExecutor.remove(siteName);
                oldExecutor.shutdown();
            } finally {
                siteLock.unlock();
            }
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger rebuildCounter = new AtomicInteger(0);
        AtomicInteger changeCounterFromLastTask = new AtomicInteger(0);
        Runnable task = () -> {
            try {
                logger.debug("Running rebuild task for site '{}'.", siteName);

                // Is the counter reach limit (watcherCounterLimit * watcherIntervalPeriod second has passed)?
                if (rebuildCounter.get() >= watcherCounterLimit) {
                    logger.debug("Counter reached '{}', rebuilding site '{}'", watcherCounterLimit, siteName);
                    rebuildContext(siteName, isFallback);
                    rebuildCounter.set(0);
                    directoryWatcherCounter.put(siteName, new AtomicInteger(0));
                    changeCounterFromLastTask.set(0);
                }

                // Check the directory watcher for new change
                AtomicInteger nextChangeCounter = directoryWatcherCounter.get(siteName);
                // Has anything else changed (more changes since we slept)?
                logger.debug("Previous changed count for site '{}' is '{}'. Current is '{}'", siteName, changeCounterFromLastTask, nextChangeCounter);
                if (nextChangeCounter != null && nextChangeCounter.get() > changeCounterFromLastTask.get()) {
                    logger.debug("Site '{}' has changed since last check, updating the change counter to '{}'.", siteName, nextChangeCounter);
                    changeCounterFromLastTask.set(nextChangeCounter.get());
                    rebuildCounter.getAndIncrement();
                } else if (nextChangeCounter != null && nextChangeCounter.get() > 0) { // there were some modified but nothing new from last check
                    logger.debug("There were some change but nothing new from last check, rebuilding site '{}'.", siteName);
                    rebuildContext(siteName, isFallback);
                    rebuildCounter.set(0);
                    changeCounterFromLastTask.set(0);
                    directoryWatcherCounter.put(siteName, new AtomicInteger(0));
                }
            } catch (Exception e) {
                logger.error("Exception while perform rebuild check for site '{}'", siteName, e);
            }
        };
        directoryWatcherExecutor.put(siteName, executor);
        executor.scheduleAtFixedRate(task, 0, watcherIntervalPeriod, TimeUnit.MILLISECONDS);
    }

    public void syncContexts() {
        logger.debug("Syncing the site contexts ...");

        Collection<String> siteNames = siteListResolver.getSiteList();

        // destroy the contexts for sites in the registry that are not present anymore (except fallback sites)
        contextRegistry.forEach((siteName, siteContext) -> {
            if (!siteContext.isFallback() && !siteNames.contains(siteName)) {
                try {
                    destroyContext(siteName);
                } catch (Exception e) {
                    logger.error("Error destroying site context for site '{}'", siteName, e);
                }
            }
        });

        // create the contexts for new sites
        siteNames.forEach(siteName -> {
            try {
                getContext(siteName, false);
            } catch (Exception e) {
                logger.error("Error creating site context for site '{}'", siteName, e);
            }
        });
    }

    /**
     * Destroys all contexts
     */
    public void destroyAllContexts() {
        logger.info("==================================================");
        logger.info("<DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");


        for (Iterator<SiteContext> iter = contextRegistry.values().iterator(); iter.hasNext();) {
            SiteContext siteContext = iter.next();
            String siteName = siteContext.getSiteName();

            logger.info("==================================================");
            logger.info("<Destroying site context: '{}'>", siteName);
            logger.info("==================================================");

            Lock siteLock = siteLockFactory.getLock(siteName);
            siteLock.lock();
            try {
                if (directoryWatcherRegistry.get(siteName) != null) {
                    DirectoryWatcher watcher = directoryWatcherRegistry.remove(siteName);
                    watcher.close();
                }
                if (directoryWatcherExecutor.get(siteName) != null) {
                    ScheduledExecutorService executor = directoryWatcherExecutor.remove(siteName);
                    executor.shutdown();
                }
                destroyContext(siteContext);
            } catch (Exception e) {
                logger.error("Error destroying site context for site '{}'", siteName, e);
            } finally {
                siteLock.unlock();
            }

            logger.info("==================================================");
            logger.info("</Destroying site context: '{}'>", siteName);
            logger.info("==================================================");

            iter.remove();
        }

        logger.info("==================================================");
        logger.info("</DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");
    }

    /**
     * Gets the {@link SiteContext} for the specified site name. If no context exists, a new one is created.
     *
     * @param siteName the context's site name
     * @param fallback if the context is a fallback (which means it will be used if no context can be resolved during
     *                 requests
     *
     * @return the context
     */
    public SiteContext getContext(@ValidSiteId String siteName, boolean fallback) {
        SiteContext siteContext = contextRegistry.get(siteName);
        if (siteContext == null) {
            if (!fallback && !siteName.equals(defaultSiteName) && !validateSiteCreationEntitlement()) {
                return null;
            }

            Lock siteLock = siteLockFactory.getLock(siteName);
            siteLock.lock();
            try {
                // Double check locking, in case the context has been created already by another thread
                siteContext = contextRegistry.get(siteName);
                if (siteContext == null) {
                    logger.info("==================================================");
                    logger.info("<Creating site context: '{}'>", siteName);
                    logger.info("==================================================");

                    siteContext = createContext(siteName, fallback);

                    if (modePreview) {
                        // files watch register
                        registerPreviewWatcher(siteName);
                        registerPreviewRebuildTask(siteName, fallback);
                    }

                    logger.info("==================================================");
                    logger.info("</Creating site context: " + siteName + ">");
                    logger.info("==================================================");
                }
            } finally {
                siteLock.unlock();
            }
        } else if (!siteContext.isValid()) {
            logger.error("Site context '{}' is not valid anymore", siteContext);

            destroyContext(siteName);

            siteContext = null;
        }

        return siteContext;
    }

    /**
     * Starts a context rebuild in the background
     *
     * @param siteName the site name of the context
     * @param fallback if the new context should be a fallback context
     */
    public void startContextRebuild(String siteName, boolean fallback) {
        startContextRebuild(siteName, fallback, null);
    }

    /**
     * Starts a context rebuild in the background
     *
     * @param siteName the site name of the context
     * @param fallback if the new context should be a fallback context
     * @param callback function to call with the new context after it has been rebuilt
     */
    public void startContextRebuild(String siteName, boolean fallback, Consumer<SiteContext> callback) {
        jobThreadPoolExecutor.execute(() -> {
            SiteContext siteContext = rebuildContext(siteName, fallback);
            if (callback != null ){
                callback.accept(siteContext);
            }
        });
    }

    /**
     * Get a site context and initializing if it does not exist.
     * If in preview mode, do not retry on failure and report the exception immediately.
     * If in live mode, retry on failure. After the max retries count, report the exception.
     * @param siteName the site name of the context
     * @return the site context
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    protected SiteContext getContextWithRetries(String siteName) throws InterruptedException {
        if (modePreview) {
            try {
                // If the site context doesn't exist (it's new), it will be created
                return getContext(siteName, false);
            } catch (Exception e) {
                logger.error(format("Error creating site context for site '%s'", siteName), e);
            }

            return null;
        }

        int attempt = 0;
        long waitTime = contextBuildRetryWaitTimeBase;
        while (attempt < contextBuildRetryMaxCount) {
            try {
                // If the site context doesn't exist (it's new), it will be created
                return getContext(siteName, false);
            } catch (Exception e) {
                attempt++;
                if (attempt == contextBuildRetryMaxCount) {
                    logger.error(format("Maximum number of retries ('%s' times) has been reached. Error creating site context for site '%s'",
                            contextBuildRetryMaxCount, siteName), e);
                    return null;
                }

                logger.warn(format("Error creating site context for site '%s'. Retrying in '%d' seconds", siteName, waitTime / 1000), e);
                Thread.sleep(waitTime);
                waitTime = waitTime * contextBuildRetryWaitTimeMultiplier;
            }
        }

        return null;
    }

    /**
     * Starts a destroy context in the background
     *
     * @param siteName the site name of the context
     */
    public void startDestroyContext(String siteName) {
        jobThreadPoolExecutor.execute(() -> destroyContext(siteName));
    }

    /**
     * Destroys the context for the specified site name, and removes it from the registry, effectively purging it
     *
     * @param siteName the site name of the context to destroy
     */
    protected void destroyContext(String siteName) {
        SiteContext siteContext;
        Lock siteLock = siteLockFactory.getLock(siteName);
        siteLock.lock();
        try {
            if (directoryWatcherRegistry.get(siteName) != null) {
                try {
                    DirectoryWatcher watcher = directoryWatcherRegistry.remove(siteName);
                    watcher.close();
                } catch (IOException e) {
                    logger.warn("Error while removing directory watcher register for site '{}'", siteName, e);
                }
            }

            if (directoryWatcherExecutor.get(siteName) != null) {
                ScheduledExecutorService executor = directoryWatcherExecutor.remove(siteName);
                executor.shutdown();
            }

            siteContext = contextRegistry.remove(siteName);
        } finally {
            siteLock.unlock();
        }

        if (siteContext != null) {
            logger.info("==================================================");
            logger.info("<Destroying site context: '{}'>", siteName);
            logger.info("==================================================");

            try {
                destroyContext(siteContext);
            } finally {
                applicationContext.publishEvent(new SiteContextPurgedEvent(siteContext));
            }

            logger.info("==================================================");
            logger.info("</Destroying site context: '{}'>", siteName);
            logger.info("==================================================");
        }
    }

    protected void destroyContexts(Collection<String> siteNames) {
        logger.info("==================================================");
        logger.info("<DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");

        if (CollectionUtils.isNotEmpty(siteNames)) {
            for (String siteName : siteNames) {
                try {
                    destroyContext(siteName);
                } catch (Exception e) {
                    logger.error("Error destroying site context for site '{}'", siteName, e);
                }
            }
        }

        logger.info("==================================================");
        logger.info("</DESTROYING SITE CONTEXTS>");
        logger.info("==================================================");
    }

    protected SiteContext createContext(String siteName, boolean fallback) {
        SiteContext siteContext;

        if (fallback) {
            siteContext = fallbackContextFactory.createContext(siteName);
            siteContext.setFallback(true);
        } else {
            siteContext = contextFactory.createContext(siteName);
        }

        siteContext.init(waitForContextInit);

        contextRegistry.put(siteName, siteContext);

        logger.info("Site context created: '{}'", siteContext);

        return siteContext;
    }

    protected SiteContext rebuildContext(String siteName, boolean fallback) {
        Lock siteLock = siteLockFactory.getLock(siteName);
        siteLock.lock();
        try {
            logger.info("==================================================");
            logger.info("<Rebuilding site context: '{}'>", siteName);
            logger.info("==================================================");

            SiteContext oldSiteContext = contextRegistry.get(siteName);
            SiteContext newContext = createContext(siteName, fallback);

            oldSiteContext.destroy();

            logger.info("==================================================");
            logger.info("</Rebuilding site context: '{}'>", siteName);
            logger.info("==================================================");

            return newContext;
        } finally {
            siteLock.unlock();
        }
    }

    protected void destroyContext(SiteContext siteContext) {
        siteContext.destroy();

        logger.info("Site context destroyed: '{}'", siteContext);
    }

    protected boolean validateSiteCreationEntitlement() {
        try {
            entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
            return true;
        } catch (EntitlementException e) {
            return false;
        }
    }

}
