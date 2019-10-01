package org.craftercms.engine.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.util.ContentStoreUtils;
import org.craftercms.engine.util.CacheUtils;
import org.craftercms.engine.util.store.decorators.DecoratedStoreAdapterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ContentStoreAdapterPreloadedFoldersBasedCacheWarmer implements ContextCacheWarmer {

    private static final Logger logger = LoggerFactory.getLogger(ContentStoreAdapterPreloadedFoldersBasedCacheWarmer.class);

    protected boolean warmUpEnabled;
    protected Map<String, Integer> descriptorPreloadFolders;
    protected Map<String, Integer> contentPreloadFolders;

    @Required
    public void setWarmUpEnabled(boolean warmUpEnabled) {
        this.warmUpEnabled = warmUpEnabled;
    }

    @Required
    public void setDescriptorPreloadFolders(String[] descriptorPreloadFolders) {
        this.descriptorPreloadFolders = CacheUtils.parsePreloadFoldersList(descriptorPreloadFolders);
    }

    @Required
    public void setContentPreloadFolders(String[] contentPreloadFolders) {
        this.contentPreloadFolders = CacheUtils.parsePreloadFoldersList(contentPreloadFolders);
    }

    @Override
    public void warmUpCache(Context context) {
        List<PreloadedFolder> preloadedFolders = new ArrayList<>();
        PreloadedFoldersAwareContext contextWrapper = findPreloadedFoldersAwareContext(context);

        if (contextWrapper == null) {
            throw new IllegalStateException("PreloadedFoldersAwareContext expected but not found");
        }

        for (Map.Entry<String, Integer> entry : contentPreloadFolders.entrySet()) {
            preloadFolder(contextWrapper, entry.getKey(), entry.getValue(), true, preloadedFolders);
        }

        for (Map.Entry<String, Integer> entry : descriptorPreloadFolders.entrySet()) {
            preloadFolder(contextWrapper, entry.getKey(), entry.getValue(), false, preloadedFolders);
        }

        contextWrapper.setPreloadedFolders(preloadedFolders);
    }

    protected PreloadedFoldersAwareContext findPreloadedFoldersAwareContext(Context context) {
        if (context instanceof PreloadedFoldersAwareContext) {
            return (PreloadedFoldersAwareContext) context;
        } else if (context instanceof DecoratedStoreAdapterContext) {
            return findPreloadedFoldersAwareContext(((DecoratedStoreAdapterContext)context).getActualContext());
        } else {
            return null;
        }
    }

    protected void preloadFolder(PreloadedFoldersAwareContext contextWrapper, String path, int depth,
                                 boolean contentOnly, List<PreloadedFolder> preloadedFolders) {
        path = ContentStoreUtils.normalizePath(path);

        Context actualContext = contextWrapper.getActualContext();
        StopWatch stopWatch = new StopWatch();

        logger.info("Starting preload of folder [{}] with depth {}", path, depth);

        stopWatch.start();

        Item rootFolder = actualContext.getStoreAdapter().findItem(actualContext, null, path, true);
        if (rootFolder == null || !rootFolder.isFolder()) {
            throw new IllegalStateException("Can't preload folder " + path + ": it doesn't exist or is not a folder");
        }

        Set<String> preloadedDescendants = new TreeSet<>();

        try {
            preloadFolderChildren(actualContext, path, depth, contentOnly, preloadedDescendants);
            preloadedFolders.add(new PreloadedFolder(path, depth, preloadedDescendants));
        } catch (Exception e) {
            logger.error("Error while preloading folder [{}]", path, e);
        }

        stopWatch.stop();

        logger.info("Preload of folder [{}] with depth {} completed in {} secs", path, depth,
                    stopWatch.getTime(TimeUnit.SECONDS));
    }

    protected void preloadFolderChildren(Context context, String path, int depth, boolean contentOnly,
                                         Set<String> preloadedPaths) {
        if (depth == ContentStoreService.UNLIMITED_TREE_DEPTH || depth >= 1) {
            if (depth >= 1) {
                depth--;
            }

            List<Item> children = context.getStoreAdapter().findItems(context, null, path);
            if (CollectionUtils.isNotEmpty(children)) {
                for (Item item : children) {
                    String childPath = item.getUrl();

                    if (item.isFolder()) {
                        logger.debug("Preloading folder [{}]", childPath);
                        if (!contentOnly) {
                            context.getStoreAdapter().findItem(context, null, childPath, true);
                        }

                        preloadedPaths.add(childPath);

                        preloadFolderChildren(context, childPath, depth, contentOnly, preloadedPaths);
                    } else if (contentOnly) {
                        logger.debug("Preloading content [{}]", childPath);
                        context.getStoreAdapter().findContent(context, null, childPath);

                        preloadedPaths.add(childPath);
                    } else {
                        logger.debug("Preloading item [{}]", childPath);
                        context.getStoreAdapter().findItem(context, null, childPath, true);

                        preloadedPaths.add(childPath);
                    }
                }
            }
        }
    }

}
