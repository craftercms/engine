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

/**
 * The strategy to handle the targeted URLs for the site.
 *
 * @author avasquez
 */
public interface TargetedUrlStrategy {

    /**
     * Returns true if the strategy is based on the name of the file.
     */
    boolean isFileNameBasedStrategy();

    /**
     * Returns the specified URL as a targeted URL (if it's not already a targeted URL) using the current target ID.
     * For example, if the specified URL is /products/index.xml, the current target ID is "en_US", and the strategy
     * handles targeted URLs by file name, then the resulting targeted URL is /products/index_en_US.xml.
     *
     * <p>
     *     <strong>WARNING: </strong> The URLs strategies should receive should be relative, without the root folder,
     *     since most targeted URLs are handled using a regex.
     * </p>
     *
     * @param url                   the URL to transform to a targeted URL
     * @param forceCurrentTargetId  true if the URL should be forced to contain the current target ID (e.g the URL
     *                              is /products/index_fr.xml but the current target ID is en, then the URL will be
     *                              transformed to /products/index_en.xml)
     *
     * @return the targeted URL version of the URL.
     */
    String toTargetedUrl(String url, boolean forceCurrentTargetId);

    /**
     * Parses the specified targeted URL, extracting it's components. For example, if the specified URL is
     * /products/index_en_US.xml, and the strategy handles targeted URLs by file name, then the URL will be
     * split into the following:
     *
     * <ul>
     *     <li><strong>Prefix:</strong> /products/index</li>
     *     <li><strong>Target ID:</strong> en_US</li>
     *     <li><strong>Suffix:</strong> .xml</li>
     * </ul>
     *
     *
     * @param targetedUrl the targeted URL to parse
     *
     * @return the URL components
     */
    TargetedUrlComponents parseTargetedUrl(String targetedUrl);

    /**
     * Builds the targeted URL with the specified prefix, target ID and suffix. For example, if the prefix is
     * /products/index, the target ID en_US, the suffix .xml, and the strategy handles targeted URLs by file name,
     * then the resulting URL will be /products/index_en_US.xml.
     *
     * @param prefix    the URL prefix
     * @param targetId  the target ID
     * @param suffix    the URL suffix
     *
     * @return the built targeted URL
     */
    String buildTargetedUrl(String prefix, String targetId, String suffix);

}
