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

import org.craftercms.engine.macro.Macro;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract {@link org.craftercms.engine.macro.Macro} that provides a macro name attribute to hold the macro name (when the macro's name
 * is variable) and the ability to skip the macro if the name is not contained in the specified string.
 *
 * @author Alfonso Vásquez
 */
public abstract class AbstractMacro implements Macro, InitializingBean {

    protected String macroName;

    public void afterPropertiesSet() {
        macroName = createMacroName();
    }

    @Override
    public String getName() {
        return macroName;
    }

    @Override
    public String resolve(String str) {
        String macroValue = getMacroValue(str);
        if (macroValue != null) {
            str = str.replace(getName(), macroValue);
        }

        return str;
    }

    protected abstract String createMacroName();

    protected abstract String getMacroValue(String str);

}
