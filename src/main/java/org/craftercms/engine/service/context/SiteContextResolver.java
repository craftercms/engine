package org.craftercms.engine.service.context;

import javax.servlet.http.HttpServletRequest;

/**
 * Resolves the {@link org.craftercms.engine.service.context.SiteContext} to be used for the specified request.
 *
 * @author avasquez
 */
public interface SiteContextResolver {

    SiteContext getContext(HttpServletRequest request);

}
