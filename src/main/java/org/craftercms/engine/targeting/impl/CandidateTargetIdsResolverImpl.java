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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.targeting.CandidateTargetIdsResolver;

/**
 * Default implementation of {@link CandidateTargetIdsResolver}, which generates the candidate target IDs by splitting
 * the original target ID and appending the components again but excluding the last one on each iteration. For example,
 * if the original target ID is ja_JP_JP and the fallback target ID is en, the the candidate targetd IDs will be:
 * ja_JP_JP, ja_JP, ja and en.
 *
 * @author avasquez
 */
public class CandidateTargetIdsResolverImpl implements CandidateTargetIdsResolver {

    public static final String DEFAULT_TARGET_ID_SEPARATOR = "_";

    protected String targetIdSeparator;

    public CandidateTargetIdsResolverImpl() {
        targetIdSeparator = DEFAULT_TARGET_ID_SEPARATOR;
    }

    public void setTargetIdSeparator(String targetIdSeparator) {
        this.targetIdSeparator = targetIdSeparator;
    }

    @Override
    public List<String> getTargetIds(String targetId, String fallbackTargetId) {
        List<String> targetIds = new ArrayList<>();
        String[] targetIdComponents = StringUtils.split(targetId, targetIdSeparator);

        targetIds.add(targetId);

        if (ArrayUtils.isNotEmpty(targetIdComponents)) {
            for (int i = targetIdComponents.length - 1; i > 0; i--) {
                targetIds.add(StringUtils.join(targetIdComponents, targetIdSeparator, 0, i));
            }
        }

        if (StringUtils.isEmpty(fallbackTargetId)) {
            targetIds.add("");
        } else if (!targetIds.contains(fallbackTargetId)) {
            targetIds.add(fallbackTargetId);
        }

        return targetIds;
    }

}
