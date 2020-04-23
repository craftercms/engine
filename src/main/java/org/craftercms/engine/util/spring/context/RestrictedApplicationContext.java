/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.spring.context;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Extension of {@link GenericApplicationContext} that only allows access to beans in the site config whitelist
 *
 * @author joseross
 * @since 3.1.7
 */
public class RestrictedApplicationContext extends GenericApplicationContext {

    public static final String CONFIG_KEY_BEAN_PATTERNS = "publicBeans.bean";

    public RestrictedApplicationContext(ApplicationContext parent) {
        super(parent);
    }

    protected boolean isAllowed(String name) {
        HierarchicalConfiguration<?> siteConfig = ConfigUtils.getCurrentConfig();
        List<String> beanPatterns = emptyList();
        if (siteConfig != null) {
            beanPatterns = siteConfig.getList(String.class, CONFIG_KEY_BEAN_PATTERNS, emptyList());
        }
        return RegexUtils.matchesAny(name, beanPatterns);
    }

    @Override
    public Object getBean(String name) throws BeansException {
        if (isAllowed(name)) {
            return super.getBean(name);
        }
        return null;
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        if (isAllowed(name)) {
            return super.getBean(name, requiredType);
        }
        return null;
    }

    @Override
    public ApplicationContext getParent() {
        // prevent direct access to the parent app context
        return null;
    }

}
