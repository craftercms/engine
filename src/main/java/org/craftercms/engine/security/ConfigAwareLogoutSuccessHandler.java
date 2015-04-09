package org.craftercms.engine.security;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.security.authentication.impl.LogoutSuccessHandlerImpl;

/**
 * Extension of {@link org.craftercms.security.authentication.impl.LogoutSuccessHandlerImpl} that uses site config
 * to override the default properties.
 *
 * @author avasquez
 */
public class ConfigAwareLogoutSuccessHandler extends LogoutSuccessHandlerImpl {

    public static final String LOGOUT_SUCCESS_URL_KEY = "security.logout.successUrl";

    @Override
    protected String getTargetUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(LOGOUT_SUCCESS_URL_KEY, targetUrl);
        } else {
            return targetUrl;
        }
    }

}
