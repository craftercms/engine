package org.craftercms.engine.util.spring;

import org.craftercms.engine.service.context.SiteContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * {@link org.springframework.core.io.ResourceLoader} that resolves paths a Crafter content store resource.
 *
 * @author avasquez
 */
public class ContentStoreResourceLoader extends DefaultResourceLoader {

    protected SiteContext siteContext;

    public ContentStoreResourceLoader(SiteContext siteContext) {
        this.siteContext = siteContext;
    }

    @Override
    protected Resource getResourceByPath(String path) {
        return new ContentStoreResource(siteContext, path);
    }

}
