package org.craftercms.engine.i10n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

/**
 * {@link CookieLocaleResolver} extension that uses the default locale of the site configuration.
 *
 * @author avasquez
 */
public class ConfigAwareCookieLocaleResolver extends CookieLocaleResolver {

    public static final String I10N_DEFAULT_LOCALE_CONFIG_KEY = "i10n.defaultLocale";

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
        if (config != null) {
            String localeStr = config.getString(I10N_DEFAULT_LOCALE_CONFIG_KEY);
            if (StringUtils.isNotEmpty(localeStr)) {
                return LocaleUtils.toLocale(localeStr);
            }
        }

        return null;
    }

}
