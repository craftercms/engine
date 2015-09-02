package org.craftercms.engine.service.context;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Abstract {@link org.craftercms.engine.service.context.SiteContextResolver} that contains common code for most
 * resolvers. Subclasses just need to resolve the site name for the current request, since this class will use the
 * site name to look for the context in the {@link SiteContextManager} (or create it if it still doesn't exist). If
 * no particular site name is resolved, then a fallback site context will be used.
 *
 * This class also creates all contexts on startup if the {@code createContextsOnStartup} flag is set. The complete
 * site list is retrieved by using a {@link SiteListResolver}. Subclasses can extend the {@link #getSiteList()} method
 * to provide the list of sites according to their implementation;
 *
 * @author avasquez
 */
public abstract class AbstractSiteContextResolver implements SiteContextResolver {

    public static final String SITE_NAME_ATTRIBUTE = "siteName";

    private static final Log logger = LogFactory.getLog(AbstractSiteContextResolver.class);

    protected boolean createContextsOnStartup;
    protected SiteContextManager siteContextManager;
    protected String fallbackSiteName;

    @Required
    public void setCreateContextsOnStartup(boolean createContextsOnStartup) {
        this.createContextsOnStartup = createContextsOnStartup;
    }

    @Required
    public void setSiteContextManager(SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    @Required
    public void setFallbackSiteName(String fallbackSiteName) {
        this.fallbackSiteName = fallbackSiteName;
    }

    @PostConstruct
    public void init() throws Exception {
        if (createContextsOnStartup) {
            createContexts();
        }
    }

    @Override
    public SiteContext getContext(HttpServletRequest request) {
        String siteName = StringUtils.lowerCase(getSiteName(request));
        boolean fallback = false;

        if (StringUtils.isNotEmpty(siteName)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Site name resolved for current request: '" + siteName + "'");
            }
        } else {
            siteName = fallbackSiteName;
            fallback = true;

            logger.warn("Unable to resolve a site name for the request. Using fallback site");
        }

        request.setAttribute(SITE_NAME_ATTRIBUTE, siteName);

        return getContext(siteName, fallback);
    }

    protected void createContexts() {
        logger.info("==================================================");
        logger.info("<CREATING SITE CONTEXTS>");
        logger.info("==================================================");

        Collection<String> siteNames = getSiteList();
        if (CollectionUtils.isNotEmpty(siteNames)) {
            for (String siteName : siteNames) {
                try {
                    siteContextManager.createContext(siteName, false);
                } catch (Exception e) {
                    logger.error("Unable to create site context for site '" + siteName + "'", e);
                }
            }
        }

        logger.info("==================================================");
        logger.info("</CREATING SITE CONTEXTS>");
        logger.info("==================================================");
    }

    protected SiteContext getContext(String siteName, boolean fallback) {
        SiteContext siteContext = siteContextManager.getContext(siteName, fallback);

        if (logger.isDebugEnabled()) {
            logger.debug("Site context resolved for current request: " + siteContext);
        }

        return siteContext;
    }

    protected abstract Collection<String> getSiteList();

    protected abstract String getSiteName(HttpServletRequest request);

}
