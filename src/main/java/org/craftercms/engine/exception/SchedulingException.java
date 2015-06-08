package org.craftercms.engine.exception;

import org.craftercms.core.exception.CrafterException;

/**
 * Thrown when a scheduling related error occurs.
 *
 * @author avasquez
 */
public class SchedulingException extends CrafterException {

    public SchedulingException(String message) {
        super(message);
    }

    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }

}
