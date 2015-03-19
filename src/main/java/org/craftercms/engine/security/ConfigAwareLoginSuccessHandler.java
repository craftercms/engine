package org.craftercms.engine.security;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.security.authentication.impl.LoginSuccessHandlerImpl;

/**
 * Extension of {@link org.craftercms.security.authentication.impl.LoginSuccessHandlerImpl} that uses site config
 * to override the default properties.
 *
 * @author avasquez
 */
public class ConfigAwareLoginSuccessHandler extends LoginSuccessHandlerImpl {

    public static final String LOGIN_DEFAULT_SUCCESS_URL_KEY = "security.login.defaultSuccessUrl";
    public static final String LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY = "security.login.alwaysUseDefaultSuccessUrl";

    @Override
    protected String getDefaultTargetUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(LOGIN_DEFAULT_SUCCESS_URL_KEY, defaultTargetUrl);
        } else {
            return defaultTargetUrl;
        }
    }

    @Override
    protected boolean isAlwaysUseDefaultTargetUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getBoolean(LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY, alwaysUseDefaultTargetUrl);
        } else {
            return alwaysUseDefaultTargetUrl;
        }
    }

}
