package org.craftercms.engine.targeting.impl;

import java.util.List;

import org.craftercms.engine.targeting.TargetIdResolver;
import org.craftercms.engine.util.spring.AbstractProxyBean;

/**
 * Created by alfonsovasquez on 17/8/15.
 */
public class ProxyTargetIdResolver extends AbstractProxyBean<TargetIdResolver> implements TargetIdResolver {

    @Override
    public String getCurrentTargetId() throws IllegalStateException {
        return getBean().getCurrentTargetId();
    }

    @Override
    public String getFallbackTargetId() throws IllegalStateException {
        return getBean().getFallbackTargetId();
    }

    @Override
    public List<String> getAvailableTargetIds() throws IllegalStateException {
        return getBean().getAvailableTargetIds();
    }

    @Override
    protected Class<? extends TargetIdResolver> getBeanClass() {
        return TargetIdResolver.class;
    }

}
