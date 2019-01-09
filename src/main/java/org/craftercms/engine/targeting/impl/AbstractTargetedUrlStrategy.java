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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link TargetedUrlStrategy} base class, that provides basic implementations of {@link #parseTargetedUrl(String)}
 * and {@link #buildTargetedUrl(String, String, String)}.
 *
 * @author avasquez
 */
public abstract class AbstractTargetedUrlStrategy implements TargetedUrlStrategy {

    protected TargetIdManager targetIdManager;

    @Required
    public void setTargetIdManager(TargetIdManager targetIdManager) {
        this.targetIdManager = targetIdManager;
    }

    @Override
    public String toTargetedUrl(String url, boolean forceCurrentTargetId) {
        Matcher matcher = matchUrl(url);
        if (matcher == null) {
            String currentTargetId = targetIdManager.getCurrentTargetId();

            return doToTargetedUrl(url, currentTargetId);
        } else if (forceCurrentTargetId) {
            String currentTargetId = targetIdManager.getCurrentTargetId();
            TargetedUrlComponents urlComponents = getTargetedUrlComponents(matcher);

            if (!currentTargetId.equals(urlComponents.getTargetId())) {
                return buildTargetedUrl(urlComponents.getPrefix(), currentTargetId, urlComponents.getSuffix());
            }
        }

        return url;
    }

    @Override
    public TargetedUrlComponents parseTargetedUrl(String targetedUrl) {
        Matcher matcher = matchUrl(targetedUrl);
        if (matcher != null) {
            return getTargetedUrlComponents(matcher);
        } else {
            return null;
        }
    }

    @Override
    public String buildTargetedUrl(String prefix, String targetId, String suffix) {
        String targetedUrl = "";

        if (StringUtils.isNotEmpty(prefix)) {
            targetedUrl += prefix;
        }
        if (StringUtils.isNotEmpty(targetId)) {
            targetedUrl += targetId;
        }
        if (StringUtils.isNotEmpty(suffix)) {
            targetedUrl += suffix;
        }

        return targetedUrl;
    }

    protected Matcher matchUrl(String url) {
        Pattern pattern = getTargetedUrlPattern();
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            return matcher;
        } else {
            return null;
        }
    }

    protected TargetedUrlComponents getTargetedUrlComponents(Matcher matcher) {
        TargetedUrlComponents urlComp = new TargetedUrlComponents();
        urlComp.setPrefix(getPrefix(matcher));
        urlComp.setTargetId(getTargetId(matcher));
        urlComp.setSuffix(getSuffix(matcher));

        return urlComp;
    }

    protected abstract String getPrefix(Matcher matcher);

    protected abstract String getTargetId(Matcher matcher);

    protected abstract String getSuffix(Matcher matcher);

    protected abstract Pattern getTargetedUrlPattern();

    protected abstract String doToTargetedUrl(String url, String currentTargetId);

}
