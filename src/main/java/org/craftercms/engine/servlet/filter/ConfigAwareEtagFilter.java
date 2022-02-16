/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.servlet.filter;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.stream.Stream;

/**
 * Extension of {@link ShallowEtagHeaderFilter} that supports custom configurations
 *
 * @author joseross
 * @since 3.1.10
 */
public class ConfigAwareEtagFilter extends ShallowEtagHeaderFilter {

    /**
     * Indicates if the filter is enabled
     */
    protected boolean enabled;

    /**
     * The list of url patterns to that should be processed
     */
    protected String[] includedUrls;

    /**
     * The {@link PathMatcher} used to compare urls
     */
    protected PathMatcher pathMatcher;

    @ConstructorProperties({"enabled", "includedUrls"})
    public ConfigAwareEtagFilter(boolean enabled, String[] includedUrls) {
        this.enabled = enabled;
        this.includedUrls = includedUrls;
        pathMatcher = new AntPathMatcher();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !enabled || Stream.of(includedUrls).noneMatch(url -> pathMatcher.match(url, request.getPathInfo()))
                || super.shouldNotFilter(request);
    }

}
