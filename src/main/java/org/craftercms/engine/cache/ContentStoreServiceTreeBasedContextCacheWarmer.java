package org.craftercms.engine.cache;

import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.util.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

public class ContentStoreServiceTreeBasedContextCacheWarmer implements ContextCacheWarmer {

    private static final Logger logger = LoggerFactory.getLogger(ContentStoreServiceTreeBasedContextCacheWarmer.class);

    protected boolean warmUpEnabled;
    protected ContentStoreService contentStoreService;
    protected Map<String, Integer> preloadFolders;

    @Required
    public void setWarmUpEnabled(boolean warmUpEnabled) {
        this.warmUpEnabled = warmUpEnabled;
    }

    @Required
    public void setContentStoreService(ContentStoreService contentStoreService) {
        this.contentStoreService = contentStoreService;
    }

    @Required
    public void setPreloadFolders(String[] preloadFolders) {
        this.preloadFolders = CacheUtils.parsePreloadFoldersList(preloadFolders);
    }

    @Override
    public void warmUpCache(Context context) {
        for (Map.Entry<String, Integer> entry : preloadFolders.entrySet()) {
            String treeRoot = entry.getKey();
            int depth = entry.getValue();

            logger.info("[{}] -> Started preload of tree {} with depth {}", context, treeRoot, depth);

            contentStoreService.getTree(context, treeRoot, depth);

            logger.info("[{}] -> Finished preload of tree {} with depth {}", context, treeRoot, depth);
        }
    }

}
