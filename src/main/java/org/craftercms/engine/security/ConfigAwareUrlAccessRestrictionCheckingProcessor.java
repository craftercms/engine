/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.engine.security;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.Callback;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ConfigUtils;
import org.craftercms.security.processors.impl.UrlAccessRestrictionCheckingProcessor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Extension of {@link org.craftercms.security.processors.impl.UrlAccessRestrictionCheckingProcessor} that uses
 * site config to override the default url restrictions.
 *
 * @author avasquez
 */
public class ConfigAwareUrlAccessRestrictionCheckingProcessor extends UrlAccessRestrictionCheckingProcessor {

    public static final String URL_RESTRICTION_KEY = "security.urlRestrictions.restriction";
    public static final String URL_RESTRICTION_URL_KEY = "url";
    public static final String URL_RESTRICTION_EXPRESSION_KEY = "expression";

    public static final String URL_RESTRICTIONS_CACHE_KEY = "urlRestrictions";

    protected CacheTemplate cacheTemplate;

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Expression> getUrlRestrictions() {
        Callback<Map<String, Expression>> callback = new Callback<Map<String, Expression>>() {

            @Override
            public Map<String, Expression> execute() {
                HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
                Map<String, Expression> customRestrictions = null;

                if (config != null) {
                    List<HierarchicalConfiguration> restrictionsConfig = config.configurationsAt(URL_RESTRICTION_KEY);
                    if (CollectionUtils.isNotEmpty(restrictionsConfig)) {
                        customRestrictions = new LinkedHashMap<>(restrictionsConfig.size());
                        ExpressionParser parser = new SpelExpressionParser();

                        for (HierarchicalConfiguration restrictionConfig : restrictionsConfig) {
                            String url = restrictionConfig.getString(URL_RESTRICTION_URL_KEY);
                            String expression = restrictionConfig.getString(URL_RESTRICTION_EXPRESSION_KEY);

                            if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(expression)) {
                                try {
                                    customRestrictions.put(url, parser.parseExpression(expression));
                                } catch (ParseException e) {
                                    throw new ConfigurationException(expression + " is not a valid SpEL expression", e);
                                }
                            }
                        }
                    }
                }

                if (customRestrictions != null) {
                    return customRestrictions;
                } else {
                    return urlRestrictions;
                }
            }

        };

        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return cacheTemplate.getObject(siteContext.getContext(), callback, URL_RESTRICTIONS_CACHE_KEY);
        } else {
            return urlRestrictions;
        }
    }

}
