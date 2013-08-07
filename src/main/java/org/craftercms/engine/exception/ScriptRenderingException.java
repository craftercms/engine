package org.craftercms.engine.exception;

/**
 * Thrown when an error occurs while rendering the response of a script.
 *
 * @author Alfonso Vásquez
 */
public class ScriptRenderingException extends ScriptException {

    public ScriptRenderingException() {
    }

    public ScriptRenderingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptRenderingException(String message) {
        super(message);
    }

    public ScriptRenderingException(Throwable cause) {
        super(cause);
    }

}
