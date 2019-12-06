/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.util.List;
import java.util.stream.Stream;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link SecurityFilterChain} that handles excluded urls
 *
 * @author joseross
 * @since 3.1.5
 */
public class DefaultSecurityFilterChain implements SecurityFilterChain {

    protected boolean securityEnabled;

    protected List<AntPathRequestMatcher> urlsToExclude;

    protected List<Filter> filters;

    public DefaultSecurityFilterChain(final boolean securityEnabled, final String[] urlsToExclude,
                                      final List<Filter> filters) {
        this.securityEnabled = securityEnabled;
        this.urlsToExclude = Stream.of(urlsToExclude).map(AntPathRequestMatcher::new).collect(toList());
        this.filters = filters;
    }

    @Override
    public boolean matches(final HttpServletRequest request) {
        return securityEnabled && urlsToExclude.stream().noneMatch(matcher -> matcher.matches(request));
    }

    @Override
    public List<Filter> getFilters() {
        return filters;
    }

}
