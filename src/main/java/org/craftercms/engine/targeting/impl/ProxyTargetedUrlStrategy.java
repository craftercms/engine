package org.craftercms.engine.targeting.impl;

import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.craftercms.engine.util.spring.AbstractProxyBean;

/**
 * Created by alfonsovasquez on 21/8/15.
 */
public class ProxyTargetedUrlStrategy extends AbstractProxyBean<TargetedUrlStrategy> implements TargetedUrlStrategy {

    @Override
    public String toTargetedUrl(String url) {
        return getBean().toTargetedUrl(url);
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
