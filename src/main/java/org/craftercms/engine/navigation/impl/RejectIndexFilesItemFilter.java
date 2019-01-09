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
package org.craftercms.engine.navigation.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;
import org.craftercms.engine.targeting.TargetedUrlComponents;
import org.craftercms.engine.targeting.TargetedUrlStrategy;
import org.craftercms.engine.properties.SiteProperties;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link ItemFilter} that rejects all indexes, including targeted ones (e.g index_en.xml, index_es_CR.xml).
 *
 * @author avasquez
 */
public class RejectIndexFilesItemFilter implements ItemFilter {

    protected TargetedUrlStrategy targetedUrlStrategy;

    @Required
    public void setTargetedUrlStrategy(TargetedUrlStrategy targetedUrlStrategy) {
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
    public boolean accepts(Item item, List<Item> acceptedItems, List<Item> rejectedItems,
                           boolean runningBeforeProcessing) {
        String indexFileName = SiteProperties.getIndexFileName();
        String itemFileName = item.getName();

        if (indexFileName.equals(itemFileName)) {
            return false;
        } else if (SiteProperties.isTargetingEnabled() && targetedUrlStrategy.isFileNameBasedStrategy()) {
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
