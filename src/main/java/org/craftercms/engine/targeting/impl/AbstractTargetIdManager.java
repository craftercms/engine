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
package org.craftercms.engine.targeting.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.engine.targeting.TargetIdManager;
import org.craftercms.engine.properties.SiteProperties;

/**
 * {@link TargetIdManager} base class, that provides basic implementations of {@link #getFallbackTargetId()} and
 * {@link #getAvailableTargetIds()}.
 *
 * @author avasquez
 */
public abstract class AbstractTargetIdManager implements TargetIdManager {

    /**
     * Returns the fallback ID defined in the current site configuration. If not defined, null is returned.
     */
    @Override
    public String getFallbackTargetId() throws IllegalStateException {
        return SiteProperties.getFallbackTargetId();
    }

    /**
     * Returns the available target IDs defined in the current site configuration. If not defined, and
     * {@link IllegalStateException} is thrown.
     */
    @Override
    public List<String> getAvailableTargetIds() {
        String[] availableTargetIds = SiteProperties.getAvailableTargetIds();
        if (ArrayUtils.isNotEmpty(availableTargetIds)) {
            return Arrays.asList(availableTargetIds);
        } else {
            throw new IllegalStateException("No available target IDs specified in the configuration");
        }
    }

}
