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
import org.craftercms.commons.lang.UrlUtils;

/**
 * {@link org.craftercms.engine.targeting.TargetedUrlStrategy} implementation that recognizes the target ID in
 * the first folder of the URL name (e.g. /en/products/index.xml).
 *
 * @author avasquez
 */
public class TargetedUrlByFolderStrategy extends AbstractTargetedUrlStrategy {

    public static final String TARGETED_URL_REGEX_FORMAT = "^/?(%s)(/.+)?$";

    public static final int TARGET_ID_GROUP = 1;
    public static final int SUFFIX_GROUP = 2;

    @Override
    public boolean isFileNameBasedStrategy() {
        return false;
    }

    @Override
    public String buildTargetedUrl(String prefix, String targetId, String suffix) {
        String targetedUrl = "";

        if (StringUtils.isNotEmpty(prefix)) {
            targetedUrl = UrlUtils.concat(targetedUrl, prefix);
        }
        if (StringUtils.isNotEmpty(targetId)) {
            targetedUrl = UrlUtils.concat(targetedUrl, targetId);
        }
        if (StringUtils.isNotEmpty(suffix)) {
            targetedUrl = UrlUtils.concat(targetedUrl, suffix);
        }

        targetedUrl = StringUtils.prependIfMissing(targetedUrl, "/");

        return targetedUrl;
    }

    @Override
    protected String getPrefix(Matcher matcher) {
        return null;
    }

    @Override
    protected String getTargetId(Matcher matcher) {
        return matcher.group(TARGET_ID_GROUP);
    }

    @Override
    protected String getSuffix(Matcher matcher) {
        return matcher.group(SUFFIX_GROUP);
    }

    @Override
    protected Pattern getTargetedUrlPattern() {
        String availableTargetIds = StringUtils.join(targetIdManager.getAvailableTargetIds(), '|');
        String targetedUrlByFilePattern = String.format(TARGETED_URL_REGEX_FORMAT, availableTargetIds);

        return Pattern.compile(targetedUrlByFilePattern);
    }

    @Override
    protected String doToTargetedUrl(String url, String currentTargetId) {
        if (StringUtils.isNotEmpty(currentTargetId)) {
            return buildTargetedUrl("", currentTargetId, url);
        } else {
            return url;
        }
    }

}
