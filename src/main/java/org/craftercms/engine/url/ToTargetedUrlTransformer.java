package org.craftercms.engine.url;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.exception.UrlTransformationException;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.Context;
import org.craftercms.core.url.UrlTransformer;
import org.craftercms.core.util.UrlUtils;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.craftercms.engine.util.TargetingUtils;
import org.craftercms.engine.util.config.TargetingProperties;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 21/8/15.
 */
public class ToTargetedUrlTransformer implements UrlTransformer {

    protected TargetedUrlStrategy targetedUrlStrategy;

    @Required
    public void setTargetedUrlStrategy(TargetedUrlStrategy targetedUrlStrategy) {
        this.targetedUrlStrategy = targetedUrlStrategy;
    }

    @Override
    public String transformUrl(Context context, CachingOptions cachingOptions,
                               String url) throws UrlTransformationException {
        if (TargetingProperties.isTargetingEnabled()) {
            String rootFolder = TargetingUtils.getMatchingRootFolder(url);
            if (StringUtils.isNotEmpty(rootFolder)) {
                String relativeUrl = StringUtils.substringAfter(url, rootFolder);
                String targetedUrl = targetedUrlStrategy.toTargetedUrl(relativeUrl);

                return UrlUtils.appendUrl(rootFolder, targetedUrl);
            }
        }

        return url;
    }

}
