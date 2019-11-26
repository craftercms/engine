package org.craftercms.engine.exception;

import org.craftercms.core.exception.CrafterException;

/**
 * Throw when  the {@link org.craftercms.engine.service.context.SiteContext} fails to initialize.
 *
 * @author avasquez
 */
public class SiteContextInitializationException extends CrafterException {

    public SiteContextInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
