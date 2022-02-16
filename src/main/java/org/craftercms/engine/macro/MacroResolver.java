/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import java.util.Map;

/**
 * Resolves the macros in the given string.
 *
 * @author Alfonso Vásquez
 *
 * @see Macro
 */
public interface MacroResolver {

    /**
     * Resolve the macros found in the string.
     *
     * @param str           the string with the macros
     *
     * @return the resolved string
     */
    String resolveMacros(String str);

    /**
     * Resolve the macros found in the string.
     *
     * @param str           the string with the macros
     * @param macroValues   additional macros values
     *
     * @return the resolved string
     */
    String resolveMacros(String str, Map<String, ?> macroValues);

}
