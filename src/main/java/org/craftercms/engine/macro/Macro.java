/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.macro;

/**
 * Represents a specific macro, and resolves any appearance of the macro in a string. A macro is a substring of the
 * form {macro} that represents a placeholder for a defined value or instruction. For example, the {webapp.path}
 * macro can be replaced for the path of the web app exploded dir.
 *
 * @author Alfonso Vásquez
 */
public interface Macro {

    String getName();

    String resolve(String str);

}
