package org.craftercms.engine.security;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.security.authentication.impl.AuthenticationRequiredHandlerImpl;

/**
 * Extension of {@link org.craftercms.security.authentication.impl.AuthenticationRequiredHandlerImpl} that uses site
 * config to override the default properties.
 *
 * @author avasquez
 */
public class ConfigAwareAuthenticationRequiredHandler extends AuthenticationRequiredHandlerImpl {

    public static final String LOGIN_FORM_URL_KEY = "security.login.formUrl";

    @Override
    protected String getLoginFormUrl() {
        HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
        if (config != null) {
            return config.getString(LOGIN_FORM_URL_KEY, loginFormUrl);
        } else {
            return loginFormUrl;
        }
    }

}
