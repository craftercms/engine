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
package org.craftercms.engine.exception;

/**
 * Thrown when a usable script view can't be found for a particular script
 *
 * @author Alfonso Vásquez
 */
public class ScriptViewNotFoundException extends ScriptRenderingException {

    public ScriptViewNotFoundException() {
    }

    public ScriptViewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptViewNotFoundException(String message) {
        super(message);
    }

    public ScriptViewNotFoundException(Throwable cause) {
        super(cause);
    }

}
