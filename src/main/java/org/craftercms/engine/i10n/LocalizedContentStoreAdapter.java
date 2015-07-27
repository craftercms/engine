package org.craftercms.engine.i10n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.AuthenticationException;
import org.craftercms.core.exception.InvalidContextException;
import org.craftercms.core.exception.StoreException;
import org.craftercms.core.exception.XmlFileParseException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.store.ContentStoreAdapter;
import org.craftercms.core.util.cache.impl.CachingAwareList;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.i18n.LocaleContextHolder;

import static org.craftercms.engine.i10n.ConfigAwareCookieLocaleResolver.*;

/**
 * {@link ContentStoreAdapter} decorator that uses localized folders to create fallback paths for searching a resource.
 * For example, if there's a request for /site/website/th_TH_TH/index.xml, the decorator will try to find the
 * resource at /site/website/th_TH/index.xml, at /site/website/th/index.xml, and if en is the default locale, at
 * /site/website/en/index.xml. If there's a locale in {@link LocaleContextHolder}, and the {@code forceCurrentLocale}
 * configuration property is set to true, it will be used instead of the locale specified in the path.
 *
 * <p>If searching for a folder's children, and the configuration property {@code mergeFolders} is set, the child
 * items of the fallback folders will be added to the result. For example, if looking for the children of folder
 * /site/website/th_TH_TH, all items that are under /site/website/th_TH, /site/website/th and /site/website/en, and
 * are not already present, will be added.</p>
 *
 * @author avasquez
 */
public class LocalizedContentStoreAdapter implements ContentStoreAdapter {

    public static final Log logger = LogFactory.getLog(LocalizedContentStoreAdapter.class);

    public static final String I10N_ENABLED_CONFIG_KEY = "i10n.enabled";
    public static final String I10N_FORCE_CURRENT_LOCALE_CONFIG_KEY = "i10n.forceCurrentLocale";
    public static final String I10N_LOCALIZED_PATHS_CONFIG_KEY = "i10n.localizedPaths";
    public static final String I10N_MERGE_FOLDERS_CONFIG_KEY = "i10n.mergeFolders";

    public static final String LOCALIZED_PATH_PATTERN_FORMAT = "(%s/)([^/.]+)(/.+)?";
    public static final int BASE_PATH_GROUP = 1;
    public static final int LOCALE_GROUP = 2;
    public static final int PATH_SUFFIX_GROUP = 3;

    protected ContentStoreAdapter actualAdapter;

    @Required
    public void setActualAdapter(ContentStoreAdapter actualAdapter) {
        this.actualAdapter = actualAdapter;
    }

    @Override
    public Context createContext(String id, String storeServerUrl, String username, String password,
                                 String rootFolderPath, boolean cacheOn, int maxAllowedItemsInCache,
                                 boolean ignoreHiddenFiles) throws StoreException, AuthenticationException {
        Context context = actualAdapter.createContext(id, storeServerUrl, username, password, rootFolderPath,
                                                      cacheOn, maxAllowedItemsInCache, ignoreHiddenFiles);

        return new ContextWrapper(this, context);
    }

    @Override
    public void destroyContext(Context context) throws InvalidContextException, StoreException,
        AuthenticationException {
        context = ((ContextWrapper)context).getActualContext();

        actualAdapter.destroyContext(context);
    }

    @Override
    public boolean exists(Context context, String path) throws InvalidContextException, StoreException {
        context = ((ContextWrapper)context).getActualContext();

        if (localizationEnabled()) {
            List<String> candidatePaths = getCandidatePaths(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    if (actualAdapter.exists(context, candidatePath)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Localized version of " + path + " found at " + candidatePath);
                        }

                        return true;
                    }
                }

                return false;
            } else {
                return actualAdapter.exists(context, path);
            }
        } else {
            return actualAdapter.exists(context, path);
        }
    }

    @Override
    public Content findContent(Context context, CachingOptions cachingOptions,
                               String path) throws InvalidContextException, StoreException {
        context = ((ContextWrapper)context).getActualContext();

        if (localizationEnabled()) {
            List<String> candidatePaths = getCandidatePaths(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    Content content = actualAdapter.findContent(context, cachingOptions, candidatePath);
                    if (content != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Localized version of " + path + " found at " + candidatePath);
                        }

                        return content;
                    }
                }

                return null;
            } else {
                return actualAdapter.findContent(context, cachingOptions, path);
            }
        } else {
            return actualAdapter.findContent(context, cachingOptions, path);
        }
    }

    @Override
    public Item findItem(Context context, CachingOptions cachingOptions, String path,
                         boolean withDescriptor) throws InvalidContextException, XmlFileParseException, StoreException {
        context = ((ContextWrapper)context).getActualContext();

        if (localizationEnabled()) {
            List<String> candidatePaths = getCandidatePaths(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                for (String candidatePath : candidatePaths) {
                    Item item = actualAdapter.findItem(context, cachingOptions, candidatePath, withDescriptor);
                    if (item != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Localized version of " + path + " found at " + candidatePath);
                        }

                        return item;
                    }
                }

                return null;
            } else {
                return actualAdapter.findItem(context, cachingOptions, path, withDescriptor);
            }
        } else {
            return actualAdapter.findItem(context, cachingOptions, path, withDescriptor);
        }
    }

    @Override
    public List<Item> findItems(Context context, CachingOptions cachingOptions, String path,
                                boolean withDescriptor) throws InvalidContextException, XmlFileParseException,
        StoreException {
        context = ((ContextWrapper)context).getActualContext();

        if (localizationEnabled()) {
            List<String> candidatePaths = getCandidatePaths(path);
            if (CollectionUtils.isNotEmpty(candidatePaths)) {
                if (mergeFolders()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Merging child items of " + candidatePaths);
                    }

                    List<Item> mergedItems = null;

                    for (String candidatePath : candidatePaths) {
                        List<Item> items = actualAdapter.findItems(context, cachingOptions, candidatePath,
                                                                   withDescriptor);
                        mergedItems = mergeItems(mergedItems, items);
                    }

                    return mergedItems;
                } else {
                    for (String candidatePath : candidatePaths) {
                        List<Item> items = actualAdapter.findItems(context, cachingOptions, candidatePath,
                                                                   withDescriptor);
                        if (CollectionUtils.isNotEmpty(items)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Localized version of " + path + " found at " + candidatePath);
                            }

                            return items;
                        }
                    }

                    return null;
                }
            } else {
                return actualAdapter.findItems(context, cachingOptions, path, withDescriptor);
            }
        } else {
            return actualAdapter.findItems(context, cachingOptions, path, withDescriptor);
        }
    }

    protected boolean localizationEnabled() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(I10N_ENABLED_CONFIG_KEY);
        } else {
            return false;
        }
    }

    protected boolean forceCurrentLocale() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(I10N_FORCE_CURRENT_LOCALE_CONFIG_KEY);
        } else {
            return false;
        }
    }

    protected String[] getLocalizedPaths() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getStringArray(I10N_LOCALIZED_PATHS_CONFIG_KEY);
        } else {
            return null;
        }
    }

    protected Locale getDefaultLocale() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            String localeStr = config.getString(I10N_DEFAULT_LOCALE_CONFIG_KEY);
            if (StringUtils.isNotEmpty(localeStr)) {
                return LocaleUtils.toLocale(localeStr);
            }
        }

        return null;
    }

    protected boolean mergeFolders() {
        Configuration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(I10N_MERGE_FOLDERS_CONFIG_KEY);
        } else {
            return false;
        }
    }

    protected Matcher getLocalizedPathMatcher(String path) {
        String[] basePaths = getLocalizedPaths();
        if (ArrayUtils.isNotEmpty(basePaths)) {
            for (String basePath : basePaths) {
                Pattern pattern = Pattern.compile(String.format(LOCALIZED_PATH_PATTERN_FORMAT, basePath));
                Matcher matcher = pattern.matcher(path);

                if (matcher.matches()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(path + " matches localized path pattern " + pattern);
                    }

                    return matcher;
                }
            }
        }

        return null;
    }

    protected List<String> getCandidatePaths(String path) {
        Matcher pathMatcher = getLocalizedPathMatcher(path);

        if (pathMatcher == null) {
            return null;
        }

        String basePath = pathMatcher.group(BASE_PATH_GROUP);
        String localeStr = pathMatcher.group(LOCALE_GROUP);
        String pathSuffix = pathMatcher.group(PATH_SUFFIX_GROUP);
        Locale locale;

        try {
            locale = LocaleUtils.toLocale(localeStr);
            if (!LocaleUtils.isAvailableLocale(locale)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(localeStr + " is not one of the available locales");
                }

                return null;
            }
        } catch (IllegalArgumentException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(localeStr + " is not a valid locale");
            }

            return null;
        }

        if (forceCurrentLocale()) {
            locale = LocaleContextHolder.getLocale();
        }

        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale == null) {
            defaultLocale = locale;
        }

        List<Locale> candidateLocales = LocaleUtils.localeLookupList(locale, defaultLocale);
        List<String> candidatePaths = new ArrayList<>(candidateLocales.size());

        for (Locale candidateLocale : candidateLocales) {
            candidatePaths.add(basePath + candidateLocale + (StringUtils.isNotEmpty(pathSuffix)? pathSuffix: ""));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Candidate paths for " + path + ": " + candidatePaths);
        }

        return candidatePaths;
    }

    protected List<Item> mergeItems(List<Item> overriding, List<Item> original) {
        if (overriding == null) {
            return original;
        } else if (original == null) {
            return overriding;
        } else {
            List<Item> merged = new CachingAwareList<>(new ArrayList<>(overriding));

            for (Item item : original) {
                if (!containsItem(merged, item)) {
                    merged.add(item);
                }
            }

            return merged;
        }
    }

    protected boolean containsItem(final List<Item> list, final Item item) {
        int idx = ListUtils.indexOf(list, new Predicate<Item>() {

            @Override
            public boolean evaluate(Item it) {
                return it.getName().equals(item.getName());
            }

        });

        return idx >= 0;
    }

    protected static class ContextWrapper implements Context {

        private LocalizedContentStoreAdapter storeAdapter;
        private Context actualContext;

        public ContextWrapper(LocalizedContentStoreAdapter storeAdapter, Context actualContext) {
            this.storeAdapter = storeAdapter;
            this.actualContext = actualContext;
        }

        public Context getActualContext() {
            return actualContext;
        }

        @Override
        public String getId() {
            return actualContext.getId();
        }

        @Override
        public ContentStoreAdapter getStoreAdapter() {
            return storeAdapter;
        }

        @Override
        public String getStoreServerUrl() {
            return actualContext.getStoreServerUrl();
        }

        @Override
        public String getRootFolderPath() {
            return actualContext.getRootFolderPath();
        }

        @Override
        public boolean isCacheOn() {
            return actualContext.isCacheOn();
        }

        @Override
        public int getMaxAllowedItemsInCache() {
            return actualContext.getMaxAllowedItemsInCache();
        }

        @Override
        public boolean ignoreHiddenFiles() {
            return actualContext.ignoreHiddenFiles();
        }

    }

}
