package org.craftercms.engine.targeting.impl.merge;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.craftercms.core.exception.XmlMergeException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategy;
import org.craftercms.core.xml.mergers.DescriptorMergeStrategyResolver;
import org.craftercms.core.xml.mergers.MergeableDescriptor;
import org.craftercms.engine.targeting.CandidateUrlsResolver;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 17/8/15.
 */
public class TargetedContentDescriptorMergeStrategy implements DescriptorMergeStrategy {

    protected DescriptorMergeStrategyResolver mergeStrategyResolver;
    protected CandidateUrlsResolver candidateUrlsResolver;

    @Required
    public void setMergeStrategyResolver(DescriptorMergeStrategyResolver mergeStrategyResolver) {
        this.mergeStrategyResolver = mergeStrategyResolver;
    }

    @Required
    public void setCandidateUrlsResolver(CandidateUrlsResolver candidateUrlsResolver) {
        this.candidateUrlsResolver = candidateUrlsResolver;
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
        List<MergeableDescriptor> results = new ArrayList<>();
        List<String> candidateUrls = candidateUrlsResolver.getUrls(mainDescriptorUrl);

        for (ListIterator<String> iter = candidateUrls.listIterator(candidateUrls.size()); iter.hasPrevious();) {
            String candidateUrl = iter.previous();
            if (!candidateUrl.equals(mainDescriptorUrl)) {
                Document descriptorDom = getDescriptorDom(context, cachingOptions, candidateUrl);
                if (descriptorDom != null) {
                    DescriptorMergeStrategy mergeStrategy = mergeStrategyResolver.getStrategy(candidateUrl,
                                                                                              descriptorDom);
                    List<MergeableDescriptor> mergeableDescriptors = mergeStrategy.getDescriptors(context,
                                                                                                  cachingOptions,
                                                                                                  candidateUrl,
                                                                                                  descriptorDom, true);

                    for (MergeableDescriptor mergeableDescriptor : mergeableDescriptors) {
                        if (!results.contains(mergeableDescriptor)) {
                            results.add(mergeableDescriptor);
                        }
                    }
                }
            } else {
                results.add(new MergeableDescriptor(candidateUrl, mainDescriptorOptional));
            }
        }

        return results;
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
