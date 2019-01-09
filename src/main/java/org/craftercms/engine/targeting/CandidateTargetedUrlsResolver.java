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
 * Resolves all the candidate targeted URLs that should be used for a specified target URL when attempting
 * content resolution.
 *
 * @author avasquez
 */
public interface CandidateTargetedUrlsResolver {

    /**
     * Resolves all the candidate targeted URLs that should be used for a given targeted URL when attempting
     * content resolution. For example, if the targeted URL is /products/index_en_US.xml, that candidate
     * URL list could look like this: /products/index_en_US.xml, /products/index_en.xml, and /products/index.xml.
     *
     * @param targetedUrl the targeted URL used to generate the candidate URLs
     *
     * @return the list of candidate targeted URLs.
     */
    List<String> getUrls(String targetedUrl);

}
