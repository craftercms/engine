package org.craftercms.engine.targeting.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.targeting.TargetIdResolver;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

/**
 * Created by alfonsovasquez on 17/8/15.
 */
public class BySiteTargetIdResolver implements TargetIdResolver {

    private static final Log logger = LogFactory.getLog(BySiteTargetIdResolver.class);

    protected TargetIdResolver defaultTargetIdResolver;
    protected String targetIdResolverBeanName;

    @Required
    public void setDefaultTargetIdResolver(TargetIdResolver defaultTargetIdResolver) {
        this.defaultTargetIdResolver = defaultTargetIdResolver;
    }

    public void setTargetIdResolverBeanName(String targetIdResolverBeanName) {
        this.targetIdResolverBeanName = targetIdResolverBeanName;
    }

    @Override
    public String getCurrentTargetId() throws IllegalStateException {
        return getCurrentTargetIdResolver().getCurrentTargetId();
    }

    @Override
    public String getDefaultTargetId() throws IllegalStateException {
        return getCurrentTargetIdResolver().getDefaultTargetId();
    }

    @Override
    public List<String> getAvailableTargetIds() throws IllegalStateException {
        return getCurrentTargetIdResolver().getAvailableTargetIds();
    }

    protected TargetIdResolver getCurrentTargetIdResolver() {
        TargetIdResolver targetIdResolver = null;
        SiteContext context = SiteContext.getCurrent();

        if (context != null) {
            ApplicationContext appContext = context.getApplicationContext();
            if (appContext != null) {
                if (StringUtils.isNotEmpty(targetIdResolverBeanName)) {
                    targetIdResolver = appContext.getBean(targetIdResolverBeanName, TargetIdResolver.class);
                } else {
                    targetIdResolver = appContext.getBean(TargetIdResolver.class);
                }
            }
        }

        if (targetIdResolver != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Site specific target ID resolver found: " + targetIdResolver);
            }

            return targetIdResolver;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No site specific target ID resolver found. Using default: " + defaultTargetIdResolver);
            }

            return defaultTargetIdResolver;
        }
    }

}
