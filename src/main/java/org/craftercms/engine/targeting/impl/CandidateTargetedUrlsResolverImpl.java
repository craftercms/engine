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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.engine.targeting.CandidateTargetIdsResolver;
import org.craftercms.engine.targeting.CandidateTargetedUrlsResolver;
import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.craftercms.engine.util.TargetingUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link CandidateTargetedUrlsResolverImpl}, that works by first extracting the root folder
 * URL of the targeted URL, then calling {@link TargetedUrlStrategy#parseTargetedUrl(String)} with the relative URL,
 * and finally build the candidate targeted URLs based on the candidate target IDs returned by
 * {@link CandidateTargetIdsResolver#getTargetIds(String, String)}.
 *
 * @author avasquez
 */
public class CandidateTargetedUrlsResolverImpl implements CandidateTargetedUrlsResolver {

    protected TargetIdManager targetIdManager;
    protected TargetedUrlStrategy targetedUrlStrategy;
    protected CandidateTargetIdsResolver candidateTargetIdsResolver;

    @Required
    public void setTargetIdManager(TargetIdManager targetIdManager) {
        this.targetIdManager = targetIdManager;
    }

    @Required
    public void setTargetedUrlStrategy(TargetedUrlStrategy targetedUrlStrategy) {
        this.targetedUrlStrategy = targetedUrlStrategy;
    }

    @Required
    public void setCandidateTargetIdsResolver(CandidateTargetIdsResolver candidateTargetIdsResolver) {
        this.candidateTargetIdsResolver = candidateTargetIdsResolver;
    }

    @Override
    public List<String> getUrls(String targetedUrl) {
        List<String> candidateUrls = new ArrayList<>();
        String rootFolder = TargetingUtils.getMatchingRootFolder(targetedUrl);

        if (StringUtils.isNotEmpty(rootFolder)) {
            String relativeTargetedUrl = StringUtils.substringAfter(targetedUrl, rootFolder);
            TargetedUrlComponents urlComp = targetedUrlStrategy.parseTargetedUrl(relativeTargetedUrl);

            if (urlComp != null) {
                String prefix = UrlUtils.concat(rootFolder, urlComp.getPrefix());
                String suffix = urlComp.getSuffix();
                String targetId = urlComp.getTargetId();
                String fallbackTargetId = targetIdManager.getFallbackTargetId();
                List<String> candidateTargetIds = candidateTargetIdsResolver.getTargetIds(targetId, fallbackTargetId);

                if (CollectionUtils.isNotEmpty(candidateTargetIds)) {
                    for (String candidateTargetId : candidateTargetIds) {
                        candidateUrls.add(targetedUrlStrategy.buildTargetedUrl(prefix, candidateTargetId, suffix));
                    }
                } else {
                    candidateUrls.add(targetedUrl);
                }
            } else {
                candidateUrls.add(targetedUrl);
            }
        } else {
            candidateUrls.add(targetedUrl);
        }

        return candidateUrls;
    }

}
