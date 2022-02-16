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
package org.craftercms.engine.util.spring.security;

import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.HeaderWriterFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.List;

/**
 * Extension of {@code org.springframework.security.web.header.HeaderWriterFilter} that only executes itself if the
 * {@code enabled} flag is set to {@code true}
 *
 * @author avasquez
 * @since 3.1.9
 */
public class ConditionalHeaderWriterFilter extends HeaderWriterFilter {

    private boolean enabled;

    @ConstructorProperties({"enabled", "headerWriters"})
    public ConditionalHeaderWriterFilter(boolean enabled, List<HeaderWriter> headerWriters) {
        super(headerWriters);
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !enabled && super.shouldNotFilter(request);
    }

}
