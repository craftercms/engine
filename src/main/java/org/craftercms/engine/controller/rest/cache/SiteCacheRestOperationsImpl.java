package org.craftercms.engine.controller.rest.cache;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.cache.CacheStatistics;
import org.craftercms.core.service.CacheService;
import org.craftercms.engine.service.context.SiteContext;

import java.beans.ConstructorProperties;

import static java.lang.String.format;

/**
 * Default implementations of {@link SiteCacheRestOperations}, used on a Crafter {@link CacheService}.
 *
 * @author avasquez
 * @since 4.3.1
 */
public class SiteCacheRestOperationsImpl implements SiteCacheRestOperations {

	public static final Log logger = LogFactory.getLog(SiteCacheRestOperationsImpl.class);

	protected CacheService cacheService;

	@ConstructorProperties({"cacheService"})
	public SiteCacheRestOperationsImpl(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Override
	public String clear(HttpServletRequest request) {
		SiteContext siteContext = getCurrentContext();
		String siteName = siteContext.getSiteName();

		cacheService.clearScope(siteContext.getContext());

		String msg = format("Cache clear for site '%s' completed", siteName);

		logger.debug(msg);

		return msg;
	}

	@Override
	public CacheStatistics getStatistics() {
		return cacheService.getStatistics(getCurrentContext().getContext());
	}

	protected SiteContext getCurrentContext() {
		SiteContext siteContext = SiteContext.getCurrent();
		if (siteContext == null) {
			throw new IllegalStateException("No current site context found");
		} else {
			return siteContext;
		}
	}
}
