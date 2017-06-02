package org.craftercms.engine.servlet.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.CookieManager;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Handler interceptor that will set the site cookie whenever the cookie is missing or the current site is different that the cookie value.
 * Use in conjunction with {@link org.craftercms.engine.service.context.CookieSiteResolver} when you want to set up simple multi tenancy.
 *
 * @author avasquez
 */
public class SiteCookieChangeInterceptor extends HandlerInterceptorAdapter {

    private static final Log logger = LogFactory.getLog(SiteCookieChangeInterceptor.class);

    protected String cookieName;

    @Required
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            String siteName = siteContext.getSiteName();
            String cookieValue = HttpUtils.getCookieValue(cookieName, request);

            if (!siteName.equals(cookieValue)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting '" + cookieName + "' cookie to '" + siteName + "'");
                }

                Cookie cookie = new Cookie(cookieName, siteName);
                cookie.setDomain(request.getServerName());
                cookie.setPath("/");
                cookie.setMaxAge(-1);

                response.addCookie(cookie);
            }
        } else {
            throw new IllegalStateException("No current site context found");
        }
    }
}
