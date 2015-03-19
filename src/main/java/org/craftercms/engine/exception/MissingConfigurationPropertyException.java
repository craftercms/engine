package org.craftercms.engine.exception;

/**
 * Thrown when a required configuration property is missing.
 *
 * @author avasquez
 */
public class MissingConfigurationPropertyException extends ConfigurationException {

    public MissingConfigurationPropertyException(String message) {
        super(message);
    }

}
