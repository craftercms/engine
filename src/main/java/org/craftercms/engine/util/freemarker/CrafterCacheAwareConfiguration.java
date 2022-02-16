/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.craftercms.engine.service.context.SiteContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;

/**
 * Extension of Freemarker's {@code Configuration} that caches the result of {@code getTemplate()} in Crafter's own
 * cache, which handles key-based smart locking so that the same template is not compiled several times by concurrent
 * threads.
 *
 * @author avasquez
 * @since 3.1.5
 */
public class CrafterCacheAwareConfiguration extends Configuration {

    protected boolean cacheTemplates;

    public CrafterCacheAwareConfiguration(Version incompatibleImprovements, boolean cacheTemplates) {
        super(incompatibleImprovements);
        this.cacheTemplates = cacheTemplates;
    }

    @Override
    public Template getTemplate(String name, Locale locale, Object customLookupCondition, String encoding,
                                boolean parseAsFTL, boolean ignoreMissing) throws IOException {
        if (cacheTemplates) {
            try {
                return SiteContext.getFromCurrentCache(() -> {
                    try {
                        return super.getTemplate(name, locale, customLookupCondition, encoding, parseAsFTL, ignoreMissing);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }, name, locale, customLookupCondition, encoding, parseAsFTL);
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        } else {
            return super.getTemplate(name, locale, customLookupCondition, encoding, parseAsFTL, ignoreMissing);
        }
    }

}
