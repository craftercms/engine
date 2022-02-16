/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.util.spring.servlet.i18n;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang.text.StrSubstitutor;
import org.craftercms.engine.util.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.singletonMap;
import static org.craftercms.commons.locale.LocaleUtils.CONFIG_KEY_SUPPORTED_LOCALES;
import static org.craftercms.commons.locale.LocaleUtils.parseLocales;
import static org.craftercms.engine.util.LocaleUtils.getCompatibleLocales;

/**
 * Implementation of {@link LocaleResolver} that uses a chain of other {@link LocaleResolver}s.
 *
 * <p>The resolvers will be executed according to the order in the configuration file, and the chain will stop
 * when one of the resolvers returns a supported locale. If none of the resolvers in the chain returns a locale the
 * default one will be used.</p>
 *
 * <p>The chain of resolvers will be executed one time for every request and the locale will be stored until the
 * response completed.</p>
 *
 * @author joseross
 * @since 4.0.0
 */
public class ChainLocaleResolver extends AbstractLocaleResolver {

    private static final Logger logger = LoggerFactory.getLogger(ChainLocaleResolver.class);

    public static final String ATTR_NAME_LOCALE = ChainLocaleResolver.class.getSimpleName() + ".LOCALE";

    public static final String BEAN_NAME_PATTERN = "crafter.${type}LocaleResolver";

    public static final String CONFIG_KEY_LOCALE_RESOLVER = "localeResolvers.localeResolver";

    public static final String CONFIG_KEY_TYPE = "type";

    /**
     * The list of supported locales
     */
    protected List<Locale> supportedLocales;

    /**
     * The list of {@link LocaleResolver}s
     */
    protected List<LocaleResolver> resolvers;

    public ChainLocaleResolver(ApplicationContext appContext, HierarchicalConfiguration<?> config) {
        setDefaultLocale(LocaleUtils.getDefaultLocale(config));

        supportedLocales = parseLocales(config.getList(String.class, CONFIG_KEY_SUPPORTED_LOCALES));

        resolvers = new LinkedList<>();
        config.configurationsAt(CONFIG_KEY_LOCALE_RESOLVER).forEach(resolverConf -> {
            String type = resolverConf.getString(CONFIG_KEY_TYPE);
            String beanName = StrSubstitutor.replace(BEAN_NAME_PATTERN, singletonMap(CONFIG_KEY_TYPE, type));
            try {
                ConfigAwareLocaleResolver resolver = (ConfigAwareLocaleResolver) appContext.getBean(beanName);
                resolver.init(resolverConf);
                resolver.setSupportedLocales(supportedLocales);
                resolvers.add(resolver);
            } catch (BeansException e) {
                logger.error("Error creating instance of bean '{}'", beanName, e);
            }
        });
    }

    protected boolean isSupported(Locale locale) {
        var compatibleLocales = getCompatibleLocales(locale);
        return supportedLocales.stream().anyMatch(compatibleLocales::contains);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale locale = (Locale) request.getAttribute(ATTR_NAME_LOCALE);
        if (locale != null) {
            logger.debug("Using previously resolved locale {}", locale);
            return locale;
        }

        logger.debug("No locale has been resolved for this request, trying to find one");
        for(LocaleResolver resolver : resolvers) {
            try {
                logger.debug("Executing locale resolver {}", resolver);
                locale = resolver.resolveLocale(request);
                if (locale != null && isSupported(locale)) {
                    logger.debug("Using new locale {}", locale);
                    return locale;
                }
            } catch (Exception e) {
                logger.error("Error during execution of locale resolver {}", resolver, e);
            }
        }

        logger.debug("No locale could be resolved, used the default locale");
        locale = getDefaultLocale();

        setLocale(request, null, locale);

        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        request.setAttribute(ATTR_NAME_LOCALE, locale);
    }

}
