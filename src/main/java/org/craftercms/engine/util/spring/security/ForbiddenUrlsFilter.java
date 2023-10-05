/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.util.spring.security;

import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.engine.exception.HttpStatusCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static java.lang.String.format;

/**
 * Simple filter that throws an {@link AccessDeniedException} if the request matches the given {@link RequestMatcher}.
 */
public class ForbiddenUrlsFilter extends GenericFilterBean {

    private final RequestMatcher matcher;

    public ForbiddenUrlsFilter(final RequestMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestUri = httpServletRequest.getRequestURI();
        if (matcher.matches(httpServletRequest)) {
            String uri = UrlUtils.cleanUrlForLog(requestUri);
            String message = format("Forbidden. You don't have permission to access '%s' on this server", uri);

            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.NOT_FOUND, message);
        }

        chain.doFilter(request, response);
    }
}
