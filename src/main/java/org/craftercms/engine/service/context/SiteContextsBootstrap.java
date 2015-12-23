package org.craftercms.engine.service.context;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Class that is used to create all contexts after Spring startup (if the {@code createContextsOnStartup} is true).
 *
 * @author avasquez
 */
public class SiteContextsBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    protected boolean createContextsOnStartup;
    protected SiteListResolver siteListResolver;
    protected SiteContextManager siteContextManager;

    protected boolean triggered;

    @Required
    public void setCreateContextsOnStartup(boolean createContextsOnStartup) {
        this.createContextsOnStartup = createContextsOnStartup;
    }

    @Required
    public void setSiteListResolver(SiteListResolver siteListResolver) {
        this.siteListResolver = siteListResolver;
    }

    @Required
    public void setSiteContextManager(SiteContextManager siteContextManager) {
        this.siteContextManager = siteContextManager;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!triggered && createContextsOnStartup) {
            triggered = true;

            siteContextManager.createContexts(siteListResolver.getSiteList());
        }
    }

}
