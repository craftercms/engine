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
package org.craftercms.engine.targeting;

import java.util.List;

/**
 * Contains methods for resolving specific target IDs.
 *
 * @author avasquez
 */
public interface TargetIdManager {

    /**
     * Returns the target ID for the current request, or null or empty if there's no target ID.
     */
    String getCurrentTargetId() throws IllegalStateException;

    /**
     * Returns the fallback target ID. The fallback target ID is used in case none of the resolved candidate targeted
     * URLs map to existing content. A null or an empty string indicates that the non-targeted version should be the
     * last candidate URL.
     *
     * <p>
     * For example, if the candidate targeted URLs are /site/website/index_es_CR.xml and /site/website/index_es.xml,
     * and the fallback target ID is "en", then /site/website/index_en.xml is added to the list of candidate URLs,
     * but if an empty or null string is the fallback target ID instead, /site/website/index.xml is the last candidate
     * URL.
     * </p>
     */
    String getFallbackTargetId() throws IllegalStateException;

    /**
     * Returns the list of all available target IDs used by the site.
     */
    List<String> getAvailableTargetIds() throws IllegalStateException;

}
