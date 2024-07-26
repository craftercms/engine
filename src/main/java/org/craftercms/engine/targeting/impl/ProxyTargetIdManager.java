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
package org.craftercms.engine.targeting.impl;

import java.util.List;

import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.util.spring.AbstractProxyBean;
import org.craftercms.engine.util.spring.ApplicationContextAccessor;

/**
 * {@link ProxyTargetIdManager} that proxies to the manager defined in the site application context, or if not
 * defined, to the default one in the general application context.
 *
 * @author avasquez
 */
public class ProxyTargetIdManager extends AbstractProxyBean<TargetIdManager> implements TargetIdManager {

    public ProxyTargetIdManager(ApplicationContextAccessor applicationContext, String beanName) {
        super(applicationContext, beanName);
    }

    @Override
    public String getCurrentTargetId() throws IllegalStateException {
        return getBean().getCurrentTargetId();
    }

    @Override
    public String getFallbackTargetId() throws IllegalStateException {
        return getBean().getFallbackTargetId();
    }

    @Override
    public List<String> getAvailableTargetIds() throws IllegalStateException {
        return getBean().getAvailableTargetIds();
    }

    @Override
    protected Class<? extends TargetIdManager> getBeanClass() {
        return TargetIdManager.class;
    }

}
