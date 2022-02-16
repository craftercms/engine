/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import org.craftercms.core.exception.ItemProcessingException;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.service.CachingOptions;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;

import java.beans.ConstructorProperties;

import static org.craftercms.engine.util.LocaleUtils.resolveLocalePath;

/**
 * Implementation of {@link ItemProcessor} that looks for a localized version of an {@link Item}
 *
 * @author joseross
 * @since 4.0.0
 */
public class LocaleItemProcessor implements ItemProcessor {

    protected ContentStoreService storeService;

    @ConstructorProperties({"storeService"})
    public LocaleItemProcessor(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public Item process(Context context, CachingOptions cachingOptions, Item item) throws ItemProcessingException {

        if (item != null && item.getDescriptorDom() != null) {
            return storeService.getItem(context,
                    resolveLocalePath(item.getDescriptorUrl(), url -> storeService.exists(context, url)));
        }

        return item;
    }

}
