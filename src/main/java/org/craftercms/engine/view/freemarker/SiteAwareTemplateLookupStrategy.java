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
package org.craftercms.engine.view.freemarker;

import freemarker.cache.TemplateLookupContext;
import freemarker.cache.TemplateLookupResult;
import freemarker.cache.TemplateLookupStrategy;

import java.io.IOException;

import static org.craftercms.commons.locale.LocaleUtils.localizePath;
import static org.craftercms.engine.util.LocaleUtils.getCompatibleLocales;

/**
 * Extension of {@link TemplateLookupStrategy} that looks for locale specific templates
 *
 * @author joseross
 * @since 4.0.0
 */
public class SiteAwareTemplateLookupStrategy extends TemplateLookupStrategy {

    @Override
    public TemplateLookupResult lookup(TemplateLookupContext ctx) throws IOException {
        var templateName = ctx.getTemplateName();

        var locales = getCompatibleLocales();

        for(var locale : locales) {
            var localizedTemplate = localizePath(templateName, locale);
            var result = ctx.lookupWithAcquisitionStrategy(localizedTemplate);
            if (result.isPositive()) {
                return result;
            }
        }

        return ctx.lookupWithAcquisitionStrategy(templateName);
    }

}
