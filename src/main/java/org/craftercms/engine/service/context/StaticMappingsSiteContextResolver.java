package org.craftercms.engine.service.context;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link org.craftercms.engine.service.context.SiteContextResolver} that resolves the current site name from a
 * set of static mappings, where the mapping key is a domain name, and the value is the site name.
 *
 * @author avasquez
 */
public class StaticMappingsSiteContextResolver extends AbstractSiteContextResolver {

    private static final Log logger = LogFactory.getLog(StaticMappingsSiteContextResolver.class);

    private Map<String, String> mappings;

    @Required
    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override
    protected Collection<String> getSiteList() {
        return new LinkedHashSet<>(mappings.values());
    }

    @Override
    protected String getSiteName(HttpServletRequest request) {
        String domainName = request.getServerName();

        if (mappings.containsKey(domainName)) {
            return mappings.get(domainName);
        } else {
            logger.warn("No site mapping found for domain name " + domainName);

            return null;
        }
    }

}
