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
 * The components of a targeted URL.
 *
 * @author avasquez
 */
public class TargetedUrlComponents {

    private String prefix;
    private String targetId;
    private String suffix;

    /**
     * Returns the URL prefix (e.g. /products/index)
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the URL prefix.
     *
     * @param prefix the prefix to set (e.g. /products/index)
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Returns the target ID (e.g. en_US)
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Sets the target ID (e.g. en_US).
     *
     * @param targetId the target ID to set
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * Returns the URL suffix (e.g. .xml)
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the URL suffix (e.g. .xml)
     *
     * @param suffix the suffix to set
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

}
