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
package org.craftercms.engine.targeting.impl;

import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.craftercms.engine.util.spring.AbstractProxyBean;

/**
 * {@link TargetedUrlStrategy} that proxies to the strategy defined in the site application context, or if not
 * defined, to the default one in the general application context.
 *
 * @author avasquez
 */
public class ProxyTargetedUrlStrategy extends AbstractProxyBean<TargetedUrlStrategy> implements TargetedUrlStrategy {

    @Override
    public boolean isFileNameBasedStrategy() {
        return getBean().isFileNameBasedStrategy();
    }

    @Override
    public String toTargetedUrl(String url, boolean forceCurrentTargetId) {
        return getBean().toTargetedUrl(url, forceCurrentTargetId);
    }

    @Override
    public TargetedUrlComponents parseTargetedUrl(String targetedUrl) {
        return getBean().parseTargetedUrl(targetedUrl);
    }

    @Override
    public String buildTargetedUrl(String prefix, String targetId, String suffix) {
        return getBean().buildTargetedUrl(prefix, targetId, suffix);
    }

    @Override
    protected Class<? extends TargetedUrlStrategy> getBeanClass() {
        return TargetedUrlStrategy.class;
    }

}
