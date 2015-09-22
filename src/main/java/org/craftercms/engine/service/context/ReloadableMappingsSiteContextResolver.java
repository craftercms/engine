package org.craftercms.engine.service.context;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
    public void init() throws Exception {
        loadMappings();

        super.init();
    }

    public synchronized void reloadMappings() throws CrafterException {
        loadMappings();
        refreshContexts();
    }

    @Override
    protected Collection<String> getSiteList() {
        Collection<Object> siteNames = mappings.values();
        Set<String> result = new LinkedHashSet<>(siteNames.size());

        for (Object siteName : siteNames) {
            result.add(siteName.toString());
        }

        return result;
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
