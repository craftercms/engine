package org.craftercms.engine.exception;

/**
 * Thrown when a script can't be found
 *
 * @author Alfonso Vásquez
 */
public class ScriptNotFoundException extends ScriptException {

    public ScriptNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
