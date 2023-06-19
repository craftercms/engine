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
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.GroovyScriptUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of {@link ConfigAwareLocaleResolver} that executes a Groovy script to resolve the locale
 *
 * <p>Supported configuration properties:</p>
 *  <ul>
 *      <li><strong>script</strong>: The path of the Groovy script in the site, defaults to
 *      {@code /scripts/locale/resolver.groovy}</li>
 *  </ul>
 *
 * @author joseross
 * @since 4.0.0
 */
public class GroovyLocaleResolver extends ConfigAwareLocaleResolver {

    public static final String CONFIG_KEY_SCRIPT = "script";
    public static final String DEFAULT_SCRIPT = "/scripts/locale/resolver.groovy";

    /**
     * The path of the Groovy script
     */
    protected String scriptPath;

    @Override
    protected void init(HierarchicalConfiguration<?> config) {
        scriptPath = config.getString(CONFIG_KEY_SCRIPT, DEFAULT_SCRIPT);
    }

    @Override
    public Locale resolveLocale(SiteContext siteContext, HttpServletRequest request) {
        Map<String, Object> variables = new HashMap<>();
        GroovyScriptUtils.addLocaleResolverScriptVariables(variables, request);

        try {
            Object result = siteContext.getScriptFactory().getScript(scriptPath).execute(variables);
            if (result != null) {
                if (!(result instanceof Locale)) {
                    throw new IllegalStateException("The returned value is not a locale object");
                }
                return (Locale) result;
            }
        } catch (Exception e) {
            logger.error("Error executing groovy locale resolver", e);
        }
        return null;
    }

}
