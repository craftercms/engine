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
package org.craftercms.engine.targeting.impl.merge;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.craftercms.core.exception.XmlMergeException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategy;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategyResolver;
import org.craftercms.core.xml.mergers.MergeableDescriptor;
import org.craftercms.core.xml.mergers.impl.strategies.InheritLevelsMergeStrategy;
import org.craftercms.engine.targeting.CandidateTargetedUrlsResolver;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link DescriptorMergeStrategy} used for merging descriptors of a targeted content hierarchy. The exact descriptors
 * to merge differ depending on the {@link org.craftercms.engine.targeting.TargetedUrlStrategy} used by the
 * {@link CandidateTargetedUrlsResolver}, but basically the list consists of the level descriptors in the folder
 * hierarchy followed by the candidate targeted URLs. For example, assuming we're using the folder targeted URL
 * strategy, the fallback target ID in the configuration is "en", and that main descriptor URL is
 * /site/website/es_CR/products/index.xml, the mergeable descriptor list will look like this:
 *
 * <ul>
 *     <li>/crafter-level-descriptor.level.xml</li>
 *     <li>/site/crafter-level-descriptor.level.xml</li>
 *     <li>/site/website/crafter-level-descriptor.level.xml</li>
 *     <li>/site/website/en/crafter-level-descriptor.level.xml</li>
 *     <li>/site/website/en/products/crafter-level-descriptor.level.xml</li>
 *     <li>/site/website/en/products/index.xml</li>
 *     <li>/site/website/es/crafter-level-descriptor.level.xml</li>
 *     <li>/site/website/es/products/crafter-level-descriptor.level.xml</li>
 *     <li>/site/website/es/products/index.xml</li>
 *     <li>/site/website/es_CR/crafter-level-descriptor.level.xml</li>
 *     <li>/site/website/es_CR/products/crafter-level-descriptor.level.xml</li>
 *     <li>/site/website/es_CR/products/index.xml</li>
 * </ul>
 *
 * @author avasquez
 */
public class TargetedContentDescriptorMergeStrategy extends InheritLevelsMergeStrategy implements
    DescriptorMergeStrategy {

    protected DescriptorMergeStrategyResolver mergeStrategyResolver;
    protected CandidateTargetedUrlsResolver candidateTargetedUrlsResolver;

    @Required
    public void setMergeStrategyResolver(DescriptorMergeStrategyResolver mergeStrategyResolver) {
        this.mergeStrategyResolver = mergeStrategyResolver;
    }

    @Required
    public void setCandidateTargetedUrlsResolver(CandidateTargetedUrlsResolver candidateTargetedUrlsResolver) {
        this.candidateTargetedUrlsResolver = candidateTargetedUrlsResolver;
    }

    @Override
    public List<MergeableDescriptor> getDescriptors(Context context, CachingOptions cachingOptions,
                                                    String mainDescriptorUrl,
                                                    Document mainDescriptorDom) throws XmlMergeException {
        return getDescriptors(context, cachingOptions, mainDescriptorUrl, mainDescriptorDom, false);
    }

    @Override
    public List<MergeableDescriptor> getDescriptors(Context context, CachingOptions cachingOptions,
                                                    String mainDescriptorUrl, Document mainDescriptorDom,
                                                    boolean mainDescriptorOptional) throws XmlMergeException {
        Set<MergeableDescriptor> results = new LinkedHashSet<>();
        List<String> candidateUrls = candidateTargetedUrlsResolver.getUrls(mainDescriptorUrl);

        for (ListIterator<String> iter = candidateUrls.listIterator(candidateUrls.size()); iter.hasPrevious();) {
            String candidateUrl = iter.previous();
            if (!candidateUrl.equals(mainDescriptorUrl)) {
                Document descriptorDom = getDescriptorDom(context, cachingOptions, candidateUrl);

                if (descriptorDom != null) {
                    DescriptorMergeStrategy mergeStrategy = mergeStrategyResolver.getStrategy(candidateUrl,
                                                                                              descriptorDom);
                    List<MergeableDescriptor> descriptors = mergeStrategy.getDescriptors(context, cachingOptions,
                                                                                         candidateUrl, descriptorDom,
                                                                                         true);

                    results.addAll(descriptors);
                }
            }
        }

        List<MergeableDescriptor> descriptors = super.getDescriptors(context, cachingOptions, mainDescriptorUrl,
                                                                     mainDescriptorDom, mainDescriptorOptional);
        results.addAll(descriptors);

        return new ArrayList<>(results);
    }

    protected Document getDescriptorDom(Context context, CachingOptions cachingOptions, String url) {
        Item item = context.getStoreAdapter().findItem(context, cachingOptions, url, true);
        if (item != null) {
            return item.getDescriptorDom();
        } else {
            return null;
        }
    }

}
