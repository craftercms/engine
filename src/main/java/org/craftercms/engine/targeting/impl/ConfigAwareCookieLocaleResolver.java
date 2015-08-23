package org.craftercms.engine.targeting.impl;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.LocaleUtils;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/**
 * {@link CookieLocaleResolver} extension that uses the default current locale specified in the site configuration if
 * the user has not current locale associated.
 *
 * @author avasquez
 */
public class ConfigAwareCookieLocaleResolver extends CookieLocaleResolver {

    public static final String DEFAULT_LOCALE_CONFIG_KEY = "defaultLocale";

    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request) {
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale != null) {
            return defaultLocale;
        } else {
            return super.determineDefaultLocale(request);
        }
    }

    protected Locale getDefaultLocale() {
        Configuration config = ConfigUtils.getCurrentConfig();
        Locale defaultLocale = LocaleUtils.toLocale(config.getString(DEFAULT_LOCALE_CONFIG_KEY));

        if (defaultLocale != null && !LocaleUtils.isAvailableLocale(defaultLocale)) {
            if (logger.isDebugEnabled()) {
                logger.debug(defaultLocale + " is not one of the available locales");
            }
        }

        return defaultLocale;
    }

}
