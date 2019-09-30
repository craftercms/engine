package org.craftercms.engine.util.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.core.exception.*;
import org.craftercms.core.service.*;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.util.ContentStoreUtils;
import org.craftercms.engine.util.store.decorators.ContentStoreAdapterDecorator;
import org.craftercms.engine.util.store.decorators.DecoratedStoreAdapterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
import java.util.function.Supplier;

public class CacheWarmingContentStoreAdapterDecorator implements ContentStoreAdapterDecorator, ContextCacheWarmer {

    private static final Logger logger = LoggerFactory.getLogger(CacheWarmingContentStoreAdapterDecorator.class);

    protected ContentStoreAdapter actualStoreAdapter;
    protected boolean warmUpEnabled;
    protected Map<String, Integer> descriptorPreloadFolders;
    protected Map<String, Integer> contentPreloadFolders;

    @Override
    public void setActualStoreAdapter(ContentStoreAdapter actualStoreAdapter) {
        this.actualStoreAdapter = actualStoreAdapter;
    }

    @Required
    public void setWarmUpEnabled(boolean warmUpEnabled) {
        this.warmUpEnabled = warmUpEnabled;
    }

    @Required
    public void setDescriptorPreloadFolders(String[] descriptorPreloadFolders) {
        this.descriptorPreloadFolders = parsePreloadFoldersList(descriptorPreloadFolders);
    }

    @Required
    public void setContentPreloadFolders(String[] contentPreloadFolders) {
        this.contentPreloadFolders = parsePreloadFoldersList(contentPreloadFolders);
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

    @Override
    public Context createContext(String id, String rootFolderPath, boolean mergingOn, boolean cacheOn,
                                 int maxAllowedItemsInCache, boolean ignoreHiddenFiles)
            throws RootFolderNotFoundException, StoreException, AuthenticationException {
        Context context = actualStoreAdapter.createContext(id, rootFolderPath, mergingOn, cacheOn,
                                                           maxAllowedItemsInCache, ignoreHiddenFiles);
        if (warmUpEnabled) {
            return new PreloadedFoldersAwareContext(context, actualStoreAdapter);
        } else {
            return context;
        }
    }

    @Override
    public boolean validate(Context context) throws StoreException, AuthenticationException {
        if (warmUpEnabled) {
            return actualStoreAdapter.validate(((PreloadedFoldersAwareContext) context).getActualContext());
        } else {
            return actualStoreAdapter.validate(context);
        }
    }

    @Override
    public void destroyContext(Context context) throws StoreException, AuthenticationException {
        if (warmUpEnabled) {
            actualStoreAdapter.destroyContext(((PreloadedFoldersAwareContext) context).getActualContext());
        } else {
            actualStoreAdapter.destroyContext(context);
        }
    }

    @Override
    public boolean exists(Context context, CachingOptions cachingOptions, String path)
            throws InvalidContextException, StoreException {
        if (warmUpEnabled) {
            PreloadedFoldersAwareContext contextWrapper = (PreloadedFoldersAwareContext) context;
            Context actualContext = contextWrapper.getActualContext();

            return executeIfNotPreloadedOrIfExistsInPreloadedPaths(contextWrapper, path, false, () ->
                    actualStoreAdapter.exists(actualContext, cachingOptions, path));
        } else {
            return actualStoreAdapter.exists(context, cachingOptions, path);
        }
    }

    @Override
    public Content findContent(Context context, CachingOptions cachingOptions, String path)
            throws InvalidContextException, StoreException {
        if (warmUpEnabled) {
            PreloadedFoldersAwareContext contextWrapper = (PreloadedFoldersAwareContext) context;
            Context actualContext = contextWrapper.getActualContext();

            return executeIfNotPreloadedOrIfExistsInPreloadedPaths(contextWrapper, path, null, () ->
                    actualStoreAdapter.findContent(actualContext, cachingOptions, path));
        } else {
            return actualStoreAdapter.findContent(context, cachingOptions, path);
        }
    }

    @Override
    public Item findItem(Context context, CachingOptions cachingOptions, String path, boolean withDescriptor)
            throws InvalidContextException, XmlFileParseException, StoreException {
        if (warmUpEnabled) {
            PreloadedFoldersAwareContext contextWrapper = (PreloadedFoldersAwareContext) context;
            Context actualContext = contextWrapper.getActualContext();

            return executeIfNotPreloadedOrIfExistsInPreloadedPaths(contextWrapper, path, null, () ->
                    actualStoreAdapter.findItem(actualContext, cachingOptions, path, withDescriptor));
        } else {
            return actualStoreAdapter.findItem(context, cachingOptions, path, withDescriptor);
        }
    }

    @Override
    public List<Item> findItems(Context context, CachingOptions cachingOptions, String path)
            throws InvalidContextException, XmlFileParseException, StoreException {
        if (warmUpEnabled) {
            PreloadedFoldersAwareContext contextWrapper = (PreloadedFoldersAwareContext) context;
            Context actualContext = contextWrapper.getActualContext();

            return executeIfNotPreloadedOrIfExistsInPreloadedPaths(contextWrapper, path, null, () ->
                    actualStoreAdapter.findItems(actualContext, cachingOptions, path));
        } else {
            return actualStoreAdapter.findItems(context, cachingOptions, path);
        }
    }

    protected Map<String, Integer> parsePreloadFoldersList(String[] preloadFolders) {
        Map<String, Integer> preloadFoldersMappings = new HashMap<>();

        for (String folder : preloadFolders) {
            String[] folderAndDepth = folder.split(":");
            if (folderAndDepth.length > 1) {
                preloadFoldersMappings.put(folderAndDepth[0], Integer.parseInt(folderAndDepth[1]));
            } else if (folderAndDepth.length == 1) {
                preloadFoldersMappings.put(folderAndDepth[0], ContentStoreService.UNLIMITED_TREE_DEPTH);
            }
        }

        return preloadFoldersMappings;
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

        Item rootFolder = actualContext.getStoreAdapter().findItem(actualContext, null, path, true);
        if (rootFolder == null || !rootFolder.isFolder()) {
            throw new IllegalStateException("Can't preload folder " + path + ": it doesn't exist or is not a folder");
        }

        Set<String> preloadedDescendants = new TreeSet<>();

        try {
            preloadFolderChildren(actualContext, path, depth, contentOnly, preloadedDescendants);
            preloadedFolders.add(new PreloadedFolder(path, depth, preloadedDescendants));
        } catch (Exception e) {
            logger.error("Error while preloading folder {}", path, e);
        }
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
                        logger.debug("[CACHE WARM UP] {} -> Preloading folder [{}]", context, childPath);
                        if (!contentOnly) {
                            context.getStoreAdapter().findItem(context, null, childPath, true);
                        }

                        preloadedPaths.add(childPath);

                        preloadFolderChildren(context, childPath, depth, contentOnly, preloadedPaths);
                    } else if (contentOnly) {
                        logger.debug("[CACHE WARM UP] {} -> Preloading content [{}]", context, childPath);
                        context.getStoreAdapter().findContent(context, null, childPath);

                        preloadedPaths.add(childPath);
                    } else {
                        logger.debug("[CACHE WARM UP] {} -> Preloading item [{}]", context, childPath);
                        context.getStoreAdapter().findItem(context, null, childPath, true);

                        preloadedPaths.add(childPath);
                    }
                }
            }
        }
    }

    protected <T> T executeIfNotPreloadedOrIfExistsInPreloadedPaths(PreloadedFoldersAwareContext contextWrapper,
                                                                    String path, T valueIfPreloadedAndNotExists,
                                                                    Supplier<T> actualCall) {
        path = ContentStoreUtils.normalizePath(path);

        PreloadedFolder preloadedAncestor = findPreloadedAncestor(contextWrapper.getPreloadedFolders(), path);
        if (preloadedAncestor != null) {
            Boolean exists = preloadedAncestor.exists(path);
            // Don't proceed if path is preloaded and doesn't exist (null means the path's level wasn't preloaded)
            if (exists != null && !exists) {
                logger.debug("Path {} not found in preloaded descendants of {}", path, preloadedAncestor);

                return valueIfPreloadedAndNotExists;
            }
        }

        return actualCall.get();
    }

    protected PreloadedFolder findPreloadedAncestor(List<PreloadedFolder> preloadedFolders, String path) {
        return preloadedFolders.stream()
                               .filter(folder -> path.startsWith(folder.getPath()))
                               .findFirst()
                               .orElse(null);
    }

    protected static class PreloadedFoldersAwareContext extends DecoratedStoreAdapterContext {

        protected List<PreloadedFolder> preloadedFolders;

        public PreloadedFoldersAwareContext(Context actualContext, ContentStoreAdapter decoratedStoreAdapter) {
            super(actualContext, decoratedStoreAdapter);
            this.preloadedFolders = Collections.emptyList();
        }

        public PreloadedFoldersAwareContext(Context actualContext, ContentStoreAdapter decoratedStoreAdapter,
                                            List<PreloadedFolder> preloadedFolders) {
            super(actualContext, decoratedStoreAdapter);
            this.preloadedFolders = preloadedFolders;
        }

        public List<PreloadedFolder> getPreloadedFolders() {
            return preloadedFolders;
        }

        public void setPreloadedFolders(List<PreloadedFolder> preloadedFolders) {
            this.preloadedFolders = preloadedFolders;
        }

        @Override
        public Context clone() {
            return new PreloadedFoldersAwareContext(actualContext.clone(), decoratedStoreAdapter, preloadedFolders);
        }

    }

}
