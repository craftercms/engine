package org.craftercms.engine.exception;

import org.craftercms.core.exception.CrafterException;

/**
 * Thrown when script related errors occur.
 */
public class ScriptException extends CrafterException {

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptException(String message) {
        super(message);
    }

}
