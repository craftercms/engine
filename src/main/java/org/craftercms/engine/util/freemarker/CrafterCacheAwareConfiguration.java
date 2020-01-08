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

    public CrafterCacheAwareConfiguration(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    public Template getTemplate(String name, Locale locale, Object customLookupCondition, String encoding,
                                boolean parseAsFTL, boolean ignoreMissing) throws IOException {
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
    }

}
