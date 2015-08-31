package org.craftercms.engine.service.context;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link org.craftercms.engine.service.context.SiteContextResolver} that resolves the current site name from an
 * extract of the request URI.
 *
 * @author avasquez
 */
public class RequestUriSiteContextResolver extends AbstractSiteContextResolver {

    private static final Log logger = LogFactory.getLog(RequestUriSiteContextResolver.class);

    protected SiteListResolver siteListResolver;
    protected String siteNameRegex;
    protected int siteNameRegexGroup;

    @Required
    public void setSiteListResolver(SiteListResolver siteListResolver) {
        this.siteListResolver = siteListResolver;
    }

    @Required
    public void setSiteNameRegex(String siteNameRegex) {
        this.siteNameRegex = siteNameRegex;
    }

    @Required
    public void setSiteNameRegexGroup(int siteNameRegexGroup) {
        this.siteNameRegexGroup = siteNameRegexGroup;
    }

    @Override
    protected Collection<String> getSiteList() {
        return siteListResolver.getSiteList();
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        Matcher matcher = Pattern.compile(siteNameRegex).matcher(requestUri);
        String siteName = null;

        if (matcher.matches()) {
            siteName = matcher.group(siteNameRegexGroup);
        } else {
            logger.warn("Unable to match request URI " + requestUri + " to regex " + siteNameRegex);
        }

        return siteName;
    }

}
