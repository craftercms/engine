package org.craftercms.engine.service.context;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.RootFolderNotFoundException;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default {@link org.craftercms.engine.service.context.SiteContextResolver}. It uses a {@link SiteListResolver} to
 * resolve the current site name.The site name is later used to look for the context in the
 * {@link SiteContextManager} (or create it if it still doesn't exist). If no particular site name is resolved, then
 * a fallback site context will be used.
 *
 * @author avasquez
 */
public class SiteContextResolverImpl implements SiteContextResolver {

    public static final String SITE_NAME_ATTRIBUTE = "siteName";

    private static final Log logger = LogFactory.getLog(SiteContextResolverImpl.class);

    protected SiteResolver siteResolver;
    protected SiteContextManager siteContextManager;
    protected String fallbackSiteName;

    @Required
    public void setSiteResolver(SiteResolver siteResolver) {
        this.siteResolver = siteResolver;
    }

    @Required
    public void setSiteContextManager(SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    @Required
    public void setFallbackSiteName(String fallbackSiteName) {
        this.fallbackSiteName = fallbackSiteName;
    }

    @Override
    public SiteContext getContext(HttpServletRequest request) {
        String siteName = StringUtils.lowerCase(siteResolver.getSiteName(request));
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

        SiteContext context;
        try {
            context = getContext(siteName, fallback);
        } catch (RootFolderNotFoundException e) {
            logger.error("Cannot resolve root folder for site '" + siteName + "'. Using fallback site", e);

            siteName = fallbackSiteName;
            fallback = true;
            context = getContext(siteName, fallback);
        }

        request.setAttribute(SITE_NAME_ATTRIBUTE, siteName);

        return context;
    }

    protected SiteContext getContext(String siteName, boolean fallback) {
        SiteContext siteContext = siteContextManager.getContext(siteName, fallback);

        if (logger.isDebugEnabled()) {
            logger.debug("Site context resolved for current request: " + siteContext);
        }

        return siteContext;
    }

}
