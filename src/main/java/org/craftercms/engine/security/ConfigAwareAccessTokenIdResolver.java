package org.craftercms.engine.security;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.profile.services.impl.AccessTokenIdResolver;
import org.craftercms.profile.services.impl.SingleAccessTokenIdResolver;

/**
 * Special {@link AccessTokenIdResolver} used in multi-tenant engine that returns the access token ID from the site
 * configuration, returning a default ID if not found.
 *
 * @author avasquez
 */
public class ConfigAwareAccessTokenIdResolver extends SingleAccessTokenIdResolver {

    public static final String ACCESS_TOKEN_ID_KEY = "profile.api.accessTokenId";

    @Override
    public String getAccessTokenId() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(ACCESS_TOKEN_ID_KEY, accessTokenId);
        } else {
            return accessTokenId;
        }
    }

}
