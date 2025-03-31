package org.craftercms.engine.controller.rest.cache;

import jakarta.servlet.http.HttpServletRequest;
import org.craftercms.core.cache.CacheStatistics;

/**
 * Set of REST operations that can be called on a site's cache (a site can have multiple types of cache).
 *
 * @author avasquez
 * @since 4.3.1
 */
public interface SiteCacheRestOperations {

	/**
	 * Clear the current site's cache
	 *
	 * @param request the current request, used to resolve the site
	 * @return the response message
	 */
	String clear(HttpServletRequest request);

	/**
	 * Get statistics for the current site's cache
	 *
	 * @return the {@link CacheStatistics}
	 */
	CacheStatistics getStatistics();

}
