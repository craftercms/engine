package org.craftercms.engine.security;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.security.authentication.impl.LoginFailureHandlerImpl;

/**
 * Extension of {@link org.craftercms.security.authentication.impl.LogoutSuccessHandlerImpl} that uses site config
 * to override the default properties.
 *
 * @author avasquez
 */
public class ConfigAwareLoginFailureHandler extends LoginFailureHandlerImpl {

    public static final String LOGIN_FAILURE_URL_KEY = "security.login.failureUrl";

    @Override
    protected String getTargetUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(LOGIN_FAILURE_URL_KEY, targetUrl);
        } else {
            return targetUrl;
        }
    }

}
