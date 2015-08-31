package org.craftercms.engine.service.context;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.HttpUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link org.craftercms.engine.service.context.SiteContextResolver} that resolves the current site name from a
 * cookie or request param.
 *
 * @author avasquez
 */
public class CookieSiteContextResolver extends AbstractSiteContextResolver {

    private static final Log logger = LogFactory.getLog(CookieSiteContextResolver.class);

    protected SiteListResolver siteListResolver;
    protected String paramOrCookieName;

    @Required
    public void setSiteListResolver(SiteListResolver siteListResolver) {
        this.siteListResolver = siteListResolver;
    }

    @Required
    public void setParamOrCookieName(String paramOrCookieName) {
        this.paramOrCookieName = paramOrCookieName;
    }

    @Override
    protected Collection<String> getSiteList() {
        return siteListResolver.getSiteList();
    }

    @Override
    protected String getSiteName(HttpServletRequest request) {
        String siteName = request.getParameter(paramOrCookieName);
        if (StringUtils.isEmpty(siteName)) {
            siteName = HttpUtils.getCookieValue(paramOrCookieName, request);
            if (StringUtils.isEmpty(siteName)) {
                logger.warn("No '" + paramOrCookieName + "' request param or cookie found");
            }
        }

        return siteName;
    }


}
