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
package org.craftercms.engine.macro.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.craftercms.engine.macro.Macro;
import org.craftercms.engine.macro.MacroResolver;

/**
 * Default implementation of {@link org.craftercms.engine.macro.MacroResolver}. Uses a chain of {@link Macro}s to
 * resolve the macros. After that, the specified additional macro values are replaced.
 *
 * @author Alfonso Vásquez
 */
public class MacroResolverImpl implements MacroResolver {

    private List<Macro> macros;

    public MacroResolverImpl(List<Macro> macros) {
        this.macros = macros;
    }

    @Override
    public String resolveMacros(String str) {
        return resolveMacros(str, null);
    }

    @Override
    public String resolveMacros(String str, Map<String, ?> macroValues) {
        if (MapUtils.isNotEmpty(macroValues)) {
            for (Map.Entry<String, ?> entry : macroValues.entrySet()) {
                String macroName = "{" + entry.getKey() + "}";
                Object macroValue = entry.getValue();

                str = str.replace(macroName, macroValue.toString());
            }
        }

        for (Macro macro : macros) {
            str = macro.resolve(str);
        }

        return str;
    }

}
