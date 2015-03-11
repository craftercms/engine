package org.craftercms.engine.service.context;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Abstract {@link org.craftercms.engine.service.context.SiteContextResolver} that contains common code for most
 * resolvers. Subclasses just need to resolve the site name for the current request, since this class will use the
 * site name to look for the context in the {@link org.craftercms.engine.service.context.SiteContextRegistry} (or
 * create it if it still doesn't exist). If no particular site name is resolved, then a fallback site context will be
 * used.
 *
 * @author avasquez
 */
public abstract class AbstractSiteContextResolver implements SiteContextResolver {

    private static final Log logger = LogFactory.getLog(AbstractSiteContextResolver.class);

    protected Lock createContextLock;

    protected SiteContextRegistry siteContextRegistry;
    protected SiteContextFactory siteContextFactory;
    protected SiteContextFactory fallbackSiteContextFactory;
    protected String fallbackSiteName;

    protected AbstractSiteContextResolver() {
        createContextLock = new ReentrantLock();
    }

    @Required
    public void setSiteContextRegistry(SiteContextRegistry siteContextRegistry) {
        this.siteContextRegistry = siteContextRegistry;
    }

    @Required
    public void setSiteContextFactory(SiteContextFactory siteContextFactory) {
        this.siteContextFactory = siteContextFactory;
    }

    @Required
    public void setFallbackSiteContextFactory(SiteContextFactory fallbackSiteContextFactory) {
        this.fallbackSiteContextFactory = fallbackSiteContextFactory;
    }

    @Required
    public void setFallbackSiteName(String fallbackSiteName) {
        this.fallbackSiteName = fallbackSiteName;
    }

    @Override
    public SiteContext getContext(HttpServletRequest request) {
        String siteName = StringUtils.lowerCase(getSiteName(request));
        if (StringUtils.isNotEmpty(siteName)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Site name resolved for current request: '" + siteName + "'");
            }
        } else {
            siteName = fallbackSiteName;

            logger.warn("Unable to resolve a site name for the request. Using fallback site");
        }

        return getContext(siteName);
    }

    protected SiteContext getContext(String siteName) {
        SiteContext context = siteContextRegistry.get(siteName);
        if (context == null) {
            context = createAndRegisterContext(siteName, false);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Site context resolved for current request: " + context);
        }

        return context;
    }

    protected SiteContext createAndRegisterContext(String siteName, boolean fallback) {
        createContextLock.lock();
        try {
            // Check again if the site context hasn't been created already by another thread
            SiteContext context = siteContextRegistry.get(siteName);
            if (context == null) {
                if (fallback) {
                    logger.info("Creating fallback site context '" + fallbackSiteName + "'...");

                    context = fallbackSiteContextFactory.createContext(siteName, true);
                } else {
                    logger.info("No context for site name '" + siteName + "' found. Creating new one...");

                    context = siteContextFactory.createContext(siteName, false);
                }

                siteContextRegistry.register(context);
            }

            return context;
        } finally {
            createContextLock.unlock();
        }
    }

    protected abstract String getSiteName(HttpServletRequest request);

}
