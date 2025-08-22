package org.craftercms.engine.controller.rest.cache;

import jakarta.servlet.http.HttpServletRequest;
import org.craftercms.core.service.CacheService;
import org.craftercms.engine.event.SiteContextCreatedEvent;
import org.craftercms.engine.event.SiteEvent;
import org.craftercms.engine.service.context.SiteContext;

import static java.lang.String.format;

/**
 * {@link SiteCacheRestOperations} for a site's internal/system cache.
 *
 * @author avasquez
 * @since 4.3.1
 */
public class SystemSiteCacheRestOperations extends SiteCacheRestOperationsImpl {

	public SystemSiteCacheRestOperations(CacheService cacheService) {
		super(cacheService);
	}

	@Override
	public String clear(HttpServletRequest request) {
		SiteContext siteContext = getCurrentContext();
		String siteName = siteContext.getSiteName();
		String msg;

		// Don't clear cache if the context was just created in this request
		if (SiteEvent.getLatestRequestEvent(SiteContextCreatedEvent.class, request) != null) {
			msg = format("Site context for '%s' created during the request. Cache clear not necessary", siteName);
		} else {
			siteContext.startCacheClear();

			msg = format("Cache clear for site '%s' started", siteName);
		}

		logger.debug(msg);

		return msg;
	}

}
