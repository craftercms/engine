package org.craftercms.engine.service.context;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.exception.CrafterException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

/**
 * {@link org.craftercms.engine.service.context.SiteResolver} that resolves the current site name from a mapping
 * of the request domain name to site name. These mappings are dynamic in that anytime while Engine is running they
 * can change (ones can be added and others removed). The {@link #reloadMappings()} method then can be called to
 * reload the mappings.
 *
 * @author avasquez
 */
public class ReloadableMappingsSiteResolver implements SiteListResolver, SiteResolver {

    private static final Log logger = LogFactory.getLog(ReloadableMappingsSiteResolver.class);

    protected Resource mappingsFile;
    protected SiteContextManager siteContextManager;

    protected volatile Properties mappings;

    @Required
    public void setMappingsFile(Resource mappingsFile) {
        this.mappingsFile = mappingsFile;
    }

    @Required
    public void setSiteContextManager(SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    @PostConstruct
    public void init() throws Exception {
        loadMappings();
    }

    public synchronized void reloadMappings() throws CrafterException {
        Collection<String> oldSiteList = getSiteList();

        loadMappings();

        Collection<String> newSiteList = getSiteList();
        Collection<String> sitesToDestroy = CollectionUtils.subtract(oldSiteList, newSiteList);

        siteContextManager.destroyContexts(sitesToDestroy);
        siteContextManager.createContexts(newSiteList);
    }

    @Override
    public Collection<String> getSiteList() {
        Collection<Object> siteNames = mappings.values();
        Set<String> result = new LinkedHashSet<>(siteNames.size());

        for (Object siteName : siteNames) {
            result.add(siteName.toString());
        }

        return result;
    }

    @Override
    public String getSiteName(HttpServletRequest request) {
        String domainName = request.getServerName();

        if (mappings.containsKey(domainName)) {
            return (String)mappings.get(domainName);
        } else {
            logger.warn("No site mapping found for domain name " + domainName);

            return null;
        }
    }

    protected void loadMappings() throws CrafterException {
        Properties tmpMappings = new Properties();

        try {
            tmpMappings.load(mappingsFile.getInputStream());
        } catch (IOException e) {
            throw new CrafterException("Unable to load domain name to site name mappings from " + mappingsFile, e);
        }

        Properties newMappings = new Properties();

        for (Map.Entry<Object, Object> entry : tmpMappings.entrySet()) {
            String trimmedKey = entry.getKey().toString().trim();
            String trimmedVal = entry.getValue().toString().trim();

            newMappings.setProperty(trimmedKey, trimmedVal);
        }

        logger.info("Domain name to site name mappings loaded from " + mappingsFile);

        mappings = newMappings;
    }

}
