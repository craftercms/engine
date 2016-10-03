/*
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.engine.navigation;

import java.util.List;

/**
 * Created by alfonsovasquez on 19/9/16.
 */
public class NavItem {

    protected String label;
    protected String url;
    protected boolean active;
    protected List<NavItem> subItems;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<NavItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<NavItem> subItems) {
        this.subItems = subItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NavItem navItem = (NavItem)o;

        if (!label.equals(navItem.label)) {
            return false;
        }

        return url.equals(navItem.url);
    }

    @Override
    public int hashCode() {
        int result = label.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NavItem{" +
               "label='" + label + '\'' +
               ", url='" + url + '\'' +
               ", active=" + active +
               ", subItems=" + subItems +
               '}';
    }

}
