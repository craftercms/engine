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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link org.craftercms.engine.targeting.TargetedUrlStrategy} implementation that recognizes the target ID in
 * the file name (e.g. /products/index_es_CR.xml).
 *
 * @author avasquez
 */
public class TargetedUrlByFileStrategy extends AbstractTargetedUrlStrategy {

    public static final String DEFAULT_TARGET_ID_SEPARATOR = "_";
    public static final String TARGETED_URL_REGEX_FORMAT = "^(.+)%s(%s)(\\.[^.]+)$";

    public static final int PREFIX_GROUP = 1;
    public static final int TARGET_ID_GROUP = 2;
    public static final int SUFFIX_GROUP = 3;

    protected String targetIdSeparator;

    public TargetedUrlByFileStrategy() {
        targetIdSeparator = DEFAULT_TARGET_ID_SEPARATOR;
    }

    public void setTargetIdSeparator(String targetIdSeparator) {
        this.targetIdSeparator = targetIdSeparator;
    }

    @Override
    public boolean isFileNameBasedStrategy() {
        return true;
    }

    @Override
    public String buildTargetedUrl(String prefix, String targetId, String suffix) {
        String targetedUrl = "";

        if (StringUtils.isNotEmpty(prefix)) {
            targetedUrl += prefix;
        }
        if (StringUtils.isNotEmpty(targetId)) {
            if (StringUtils.isNotEmpty(targetedUrl)) {
                targetedUrl = StringUtils.appendIfMissing(targetedUrl, targetIdSeparator);
            }

            targetedUrl += targetId;
        }
        if (StringUtils.isNotEmpty(suffix)) {
            targetedUrl += StringUtils.prependIfMissing(suffix, ".");
        }

        targetedUrl = StringUtils.prependIfMissing(targetedUrl, "/");

        return targetedUrl;
    }

    @Override
    protected String getPrefix(Matcher matcher) {
        return matcher.group(PREFIX_GROUP);
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
        String targetedUrlByFilePattern = String.format(TARGETED_URL_REGEX_FORMAT, targetIdSeparator,
                                                        availableTargetIds);

        return Pattern.compile(targetedUrlByFilePattern);
    }

    @Override
    protected String doToTargetedUrl(String url, String currentTargetId) {
        String ext = FilenameUtils.getExtension(url);
        if (StringUtils.isNotEmpty(ext)) {
            String urlWithoutExt = FilenameUtils.removeExtension(url);

            return buildTargetedUrl(urlWithoutExt, currentTargetId, ext);
        } else {
            return url;
        }
    }

}
