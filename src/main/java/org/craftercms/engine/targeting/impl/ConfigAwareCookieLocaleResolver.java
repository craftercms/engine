package org.craftercms.engine.targeting.impl;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.util.config.TargetingProperties;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/**
 * {@link CookieLocaleResolver} extension that uses the default target ID of the site configuration as the current
 * locale, if it's a valid and available locale.
 *
 * @author avasquez
 */
public class ConfigAwareCookieLocaleResolver extends CookieLocaleResolver {

    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request) {
        Locale defaultLocale = null;
        String defaultTargetId = TargetingProperties.getDefaultTargetId();

        if (StringUtils.isNotEmpty(defaultTargetId)) {
            try {
                defaultLocale = LocaleUtils.toLocale(defaultTargetId);
                if (!LocaleUtils.isAvailableLocale(defaultLocale)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(defaultTargetId + " is not one of the available locales");
                    }

                    defaultLocale = null;
                }
            } catch (IllegalArgumentException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(defaultTargetId + " is not a valid locale");
                }
            }
        }

        if (defaultLocale != null) {
            return defaultLocale;
        } else {
            return super.determineDefaultLocale(request);
        }
    }

}
