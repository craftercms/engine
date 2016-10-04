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
package org.craftercms.engine.navigation.impl;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.craftercms.engine.util.config.CommonProperties;
import org.craftercms.engine.util.config.TargetingProperties;

/**
 * Created by alfonsovasquez on 28/9/16.
 */
public class IgnoreIndexFilesItemFilter implements ItemFilter {

    protected TargetedUrlStrategy targetedUrlStrategy;

    public IgnoreIndexFilesItemFilter(TargetedUrlStrategy targetedUrlStrategy) {
        this.targetedUrlStrategy = targetedUrlStrategy;
    }

    @Override
    public boolean runBeforeProcessing() {
        return true;
    }

    @Override
    public boolean runAfterProcessing() {
        return false;
    }

    @Override
    public boolean accepts(Item item, boolean runningBeforeProcessing) {
        String indexFileName = CommonProperties.getIndexFileName();
        String itemFileName = item.getName();

        if (indexFileName.equals(itemFileName)) {
            return false;
        } else if (TargetingProperties.isTargetingEnabled() && targetedUrlStrategy.isFileNameBasedStrategy()) {
            TargetedUrlComponents targetedFileNameComponents = targetedUrlStrategy.parseTargetedUrl(itemFileName);
            if (targetedFileNameComponents != null) {
                String prefix = targetedFileNameComponents.getPrefix();
                String suffix = targetedFileNameComponents.getSuffix();
                String nonTargetedItemFileName = targetedUrlStrategy.buildTargetedUrl(prefix, null, suffix);

                nonTargetedItemFileName = StringUtils.strip(nonTargetedItemFileName, "/");

                if (indexFileName.equals(nonTargetedItemFileName)) {
                    return false;
                }
            }
        }

        return true;
    }

}
