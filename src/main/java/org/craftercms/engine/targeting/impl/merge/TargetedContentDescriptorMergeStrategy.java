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
 * Created by alfonsovasquez on 17/8/15.
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
