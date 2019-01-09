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
package org.craftercms.engine.macro.impl;

import org.springframework.beans.factory.annotation.Required;

/**
 * Created by alfonsovasquez on 14/10/16.
 */
public class FixedValueMacro extends AbstractMacro {

    private String macroName;
    private String macroValue;

    @Required
    public void setMacroName(String macroName) {
        this.macroName = macroName;
    }

    @Required
    public void setMacroValue(String macroValue) {
        this.macroValue = macroValue;
    }

    @Override
    protected String createMacroName() {
        return "{" + macroName + "}";
    }

    @Override
    protected String getMacroValue(String str) {
        return macroValue;
    }

}
