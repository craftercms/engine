package org.craftercms.engine.security;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.security.authorization.impl.AccessDeniedHandlerImpl;

/**
 * Extension of {@link org.craftercms.security.authentication.impl.AuthenticationRequiredHandlerImpl} that uses site
 * config to override the default properties.
 *
 * @author avasquez
 */
public class ConfigAwareAccessDeniedHandler extends AccessDeniedHandlerImpl {

    public static final String ACCESS_DENIED_ERROR_PAGE_URL_KEY = "security.accessDenied.errorPageUrl";

    @Override
    protected String getErrorPageUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(ACCESS_DENIED_ERROR_PAGE_URL_KEY, errorPageUrl);
        } else {
            return errorPageUrl;
        }
    }

}
