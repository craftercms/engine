package org.craftercms.engine.service.context;

import javax.servlet.http.HttpServletRequest;

/**
 * Resolves the site for a given request.
 *
 * @author avasquez
 */
public interface SiteResolver {

    String getSiteName(HttpServletRequest request);

}
