package org.craftercms.engine.exception;

import org.craftercms.core.exception.CrafterException;

/**
 * Thrown when an error occurs while trying to read a site's configuration.
 *
 * @author avasquez
 */
public class ConfigurationException extends CrafterException {

    public ConfigurationException() {
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}
