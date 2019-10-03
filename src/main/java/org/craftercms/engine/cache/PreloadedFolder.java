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
package org.craftercms.engine.cache;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.service.ContentStoreService;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a folder in the content store that has been preloaded in the cache.
 *
 * @author avasquez
 * @since 3.1.4
 */
public class PreloadedFolder {

    private String path;
    private int depth;
    private Set<String> descendants;

    public PreloadedFolder(String path, int depth, Set<String> descendants) {
        this.path = StringUtils.appendIfMissing(path, "/");
        this.depth = depth;
        this.descendants = descendants;
    }

    /**
     * Returns the path of the folder.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns:
     *
     * <ul>
     *     <li>Null if the descendant's depth is greater than the preload depth for this folder.</li>
     *     <li>True if the descendant is in the list of preloaded descendants</li>
     *     <li>False if the descendant is not in the list of preloaded descendants</li>
     * </ul>
     */
    public Boolean exists(String descendant) {
        if (depth == ContentStoreService.UNLIMITED_TREE_DEPTH) {
            return descendants.contains(descendant);
        } else {
            int childDepth = getDepth(descendant);
            if (childDepth > depth) {
                return null;
            } else {
                return descendants.contains(descendant);
            }
        }
    }

    private int getDepth(String child) {
        String afterParentPath = StringUtils.substringAfter(child, path);
        String[] pathComponents = afterParentPath.split("/");

        return pathComponents.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreloadedFolder that = (PreloadedFolder) o;

        return depth == that.depth && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, depth);
    }

    @Override
    public String toString() {
        return "PreloadedFolder{" +
               "path='" + path + '\'' +
               ", depth=" + depth +
               '}';
    }

}
