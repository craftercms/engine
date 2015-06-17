package org.craftercms.engine.service.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.CrafterException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

/**
 * {@link org.craftercms.engine.service.context.SiteContextResolver} that resolves the current site name from a mapping
 * of the request domain name to site name. These mappings are dynamic in that anytime while Engine is running they
 * can change (ones can be added and others removed). The {@link #reloadMappings()} method then can be called to
 * reload the mappings.
 *
 * @author avasquez
 */
public class ReloadableMappingsSiteContextResolver extends AbstractSiteContextResolver {

    private static final Log logger = LogFactory.getLog(ReloadableMappingsSiteContextResolver.class);

    protected Resource mappingsFile;
    protected volatile Properties mappings;

    @Required
    public void setMappingsFile(Resource mappingsFile) {
        this.mappingsFile = mappingsFile;
    }

    @PostConstruct
    public void init() {
        loadMappings();
    }

    public void reloadMappings() throws CrafterException {
        loadMappings();
        destroyContextsWithNoMapping();
    }

    @Override
    protected String getSiteName(HttpServletRequest request) {
        String domainName = request.getServerName();

        if (mappings.containsKey(domainName)) {
            return (String)mappings.get(domainName);
        } else {
            logger.warn("No site mapping found for domain name " + domainName);

            return null;
        }
    }

    protected void loadMappings() throws CrafterException {
        Properties newMappings = new Properties();

        try {
            newMappings.load(mappingsFile.getInputStream());
        } catch (IOException e) {
            throw new CrafterException("Unable to load domain name to site name mappings from " + mappingsFile, e);
        }

        logger.info("Domain name to site name mappings loaded from " + mappingsFile);

        mappings = newMappings;
    }

    protected void destroyContextsWithNoMapping() {
        List<SiteContext> contextsToUnregister = new ArrayList<>();
        Collection<Object> currentSiteNames = mappings.values();

        for (SiteContext siteContext : siteContextManager.listContexts()) {
            String siteName = siteContext.getSiteName();
            if (!siteName.equals(fallbackSiteName) && !currentSiteNames.contains(siteName)) {
                contextsToUnregister.add(siteContext);
            }
        }

        for (SiteContext siteContext : contextsToUnregister) {
            siteContextManager.destroyContext(siteContext.getSiteName());
        }
    }

}
