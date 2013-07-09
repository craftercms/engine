/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.crafter.engine.exception;

import org.craftercms.crafter.core.exception.CrafterException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown to cause specific HTTP status code errors
 *
 * @author Alfonso Vásquez
 * @see org.craftercms.crafter.engine.http.impl.HttpStatusCodeExceptionHandler
 */
public class HttpStatusCodeException extends CrafterException {

    private HttpStatus statusCode;

    public HttpStatusCodeException(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public HttpStatusCodeException(HttpStatus statusCode, String message, Throwable cause) {
        super(message, cause);

        this.statusCode = statusCode;
    }

    public HttpStatusCodeException(HttpStatus statusCode, String message) {
        super(message);

        this.statusCode = statusCode;
    }

    public HttpStatusCodeException(HttpStatus statusCode, Throwable cause) {
        super(cause);

        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        String clazz = getClass().getName();
        String message = getLocalizedMessage();

        if (message != null) {
            message = "[" + statusCode.getReasonPhrase() + "] " + message;
        } else {
            message = "[" + statusCode.getReasonPhrase() + "]";
        }

        return clazz + ": " + message;
    }

}
