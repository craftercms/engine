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
package org.craftercms.crafter.engine.macro.impl;

import org.craftercms.crafter.engine.macro.Macro;
import org.craftercms.crafter.engine.macro.MacroResolver;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Default implementation of {@link MacroResolver}. Uses a chain of {@link Macro}s to resolve the macros.
 *
 * @author Alfonso Vásquez
 */
public class MacroResolverImpl implements MacroResolver {

    private List<Macro> macros;

    @Required
    public void setMacros(List<Macro> macros) {
        this.macros = macros;
    }

    @Override
    public String resolveMacros(String path) {
        for (Macro macro : macros) {
            path = macro.resolve(path);
        }

        return path;
    }

}
