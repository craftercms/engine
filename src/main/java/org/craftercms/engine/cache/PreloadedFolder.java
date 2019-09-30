package org.craftercms.engine.cache;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.service.ContentStoreService;

import java.util.Objects;
import java.util.Set;

public class PreloadedFolder {

    private String path;
    private int depth;
    private Set<String> descendants;

    public PreloadedFolder(String path, int depth, Set<String> descendants) {
        this.path = StringUtils.appendIfMissing(path, "/");
        this.depth = depth;
        this.descendants = descendants;
    }

    public String getPath() {
        return path;
    }

    public Boolean exists(String child) {
        if (depth == ContentStoreService.UNLIMITED_TREE_DEPTH) {
            return descendants.contains(child);
        } else {
            int childDepth = getDepth(child);
            if (childDepth > depth) {
                return null;
            } else {
                return descendants.contains(child);
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
