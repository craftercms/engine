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
package org.craftercms.engine.util.predicates;

import java.util.Date;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.converters.Converter;
import org.craftercms.core.service.Item;
import org.springframework.beans.factory.annotation.Required;

/**
 * Predicate used to check if an item is expired.
 *
 * @author avasquez
 */
public class ExpiredItemPredicate implements Predicate<Item> {

    private static final Log logger = LogFactory.getLog(ExpiredItemPredicate.class);

    protected String expiredXPathQuery;
    protected Converter<String, Date> dateConverter;

    @Required
    public void setExpiredXPathQuery(String expiredXPathQuery) {
        this.expiredXPathQuery = expiredXPathQuery;
    }

    @Required
    public void setDateConverter(Converter<String, Date> dateConverter) {
        this.dateConverter = dateConverter;
    }

    @Override
    public boolean evaluate(Item item) {
        String expired = item.queryDescriptorValue(expiredXPathQuery);
        if (StringUtils.isNotEmpty(expired)) {
            Date expiredDate = dateConverter.convert(expired);
            Date now = new Date();

            if (now.before(expiredDate)) {
                return true;
            } else {
                logger.info("Item " + item.getDescriptorUrl() + " has expired");

                return false;
            }
        } else {
            return true;
        }
    }
    
}
