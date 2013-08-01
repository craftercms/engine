package org.craftercms.engine.exception;

import org.craftercms.core.exception.CrafterException;

/**
 * Thrown when script related errors occur.
 */
public class ScriptException extends CrafterException {

    public ScriptException() {
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(Throwable cause) {
        super(cause);
    }

}
