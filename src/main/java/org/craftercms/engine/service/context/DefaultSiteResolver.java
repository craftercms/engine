package org.craftercms.engine.service.context;

import java.util.Collection;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves always the current site name to a default
 * site name.
 *
 * @author avasquez
 */
public class DefaultSiteResolver implements SiteListResolver, SiteResolver {

    private String defaultSiteName;

    @Required
    public void setDefaultSiteName(String defaultSiteName) {
        this.defaultSiteName = defaultSiteName;
    }

    @Override
    public Collection<String> getSiteList() {
        return Collections.singleton(defaultSiteName);
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        return defaultSiteName;
    }

}
