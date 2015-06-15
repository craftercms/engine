package org.craftercms.engine.servlet.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.craftercms.core.exception.CrafterException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextResolver;
import org.springframework.beans.factory.annotation.Required;

/**
 * Filter that uses a {@link org.craftercms.engine.service.context.SiteContextResolver} to resolve the context for
 * the current request. The site context and the site name are then set as request attributes.
 *
 * @author avasquez
 */
public class SiteContextResolvingFilter implements Filter {

    protected SiteContextResolver contextResolver;

    @Required
    public void setContextResolver(SiteContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        SiteContext siteContext = contextResolver.getContext((HttpServletRequest)request);

        if (siteContext == null) {
            throw new CrafterException("No site context was resolved for the current request");
        }

        SiteContext.setCurrent(siteContext);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

}
