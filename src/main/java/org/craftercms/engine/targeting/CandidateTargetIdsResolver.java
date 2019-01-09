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
 * Resolves the target IDs to use to build the candidate targeted URLs.
 *
 * @author avasquez
 */
public interface CandidateTargetIdsResolver {

    /**
     * Resolves the target IDs to use to build the candidate targeted URLs. For example, if the specified target ID
     * is es_CR, and the fallback target ID is en, then the candidate target IDs are: es_CR, es and en. An empty
     * string will be added if the fallback target ID is empty or null.
     *
     * @param targetId          the target ID from where to resolve the candidate target IDs
     * @param fallbackTargetId  the last candidate target ID to use
     *
     * @return the list of candidate target IDs
     */
    List<String> getTargetIds(String targetId, String fallbackTargetId);

}
