package org.craftercms.engine.security;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.profile.services.impl.AccessTokenIdResolver;

/**
 * Special {@link AccessTokenIdResolver} used in multi-tenant engine that returns the access token ID from the site
 * configuration, throwing an {@link ConfigurationException} if not found.
 *
 * @author avasquez
 */
public class ConfigAwareAccessTokenIdResolver implements AccessTokenIdResolver {

    public static final String ACCESS_TOKEN_ID_KEY = "profile.api.accessTokenId";

    @Override
    public String getAccessTokenId() {
        String accessTokenId = null;
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();

        if (config != null) {
            accessTokenId = config.getString(ACCESS_TOKEN_ID_KEY);
        }

        if (StringUtils.isNotEmpty(accessTokenId)) {
            return accessTokenId;
        } else {
            throw new ConfigurationException("Current config for site '" + SiteContext.getCurrent().getSiteName() +
                                            "' doesn't contain required property " + ACCESS_TOKEN_ID_KEY);
        }
    }

}
