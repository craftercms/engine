package org.craftercms.engine.exception;

import org.craftercms.core.exception.CrafterException;

/**
 * Thrown when an invalid cache type has been specified as a request parameter
 *
 * @author avasquez
 * @since 4.3.1
 */
public class InvalidCacheTypeException extends CrafterException {

	public InvalidCacheTypeException(String message) {
		super(message);
	}

}
