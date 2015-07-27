package org.craftercms.engine.i10n;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.craftercms.engine.util.config.I10nProperties;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/**
 * {@link CookieLocaleResolver} extension that uses the default locale of the site configuration.
 *
 * @author avasquez
 */
public class ConfigAwareCookieLocaleResolver extends CookieLocaleResolver {

    @Override
    protected Locale determineDefaultLocale(HttpServletRequest request) {
        Locale defaultLocale = I10nProperties.getDefaultLocale();
        if (defaultLocale != null) {
            return defaultLocale;
        } else {
            return super.determineDefaultLocale(request);
        }
    }

}
