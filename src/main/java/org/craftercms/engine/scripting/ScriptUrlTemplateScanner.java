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

package org.craftercms.engine.scripting;

import java.util.List;

import org.craftercms.engine.service.context.SiteContext;
import org.springframework.web.util.UriTemplate;

/**
 * Scans the site context for scripts that have URL variables, for example, /scripts/rest/user/{username}.get.json.
 *
 * @author avasquez.
 */
public interface ScriptUrlTemplateScanner {

    /**
     * Scans the site context at a certain path to discover script URL templates.
     *
     * @param siteContext the site context to scan
     *
     * @return the list of URL templates
     */
    List<UriTemplate> scan(SiteContext siteContext);

}
