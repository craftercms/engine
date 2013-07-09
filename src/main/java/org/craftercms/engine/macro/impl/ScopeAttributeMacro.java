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
package org.craftercms.engine.macro.impl;

import org.craftercms.core.util.HttpServletUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Represents a macro that can be an attribute from the request or session scope.
 *
 * @author Alfonso Vásquez
 */
public class ScopeAttributeMacro extends AbstractMacro {

    private String attributeName;
    private boolean requestScope;

    public ScopeAttributeMacro() {
        requestScope = true;
    }

    @Required
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setRequestScope(boolean requestScope) {
        this.requestScope = requestScope;
    }

    @Override
    protected String createMacroName() {
        return "{" + attributeName + "}";
    }

    @Override
    protected String getMacroValue(String str) {
        int scope = requestScope? HttpServletUtils.SCOPE_REQUEST : HttpServletUtils.SCOPE_SESSION;

        return (String) HttpServletUtils.getAttribute(attributeName, scope);
    }

}
