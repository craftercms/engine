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
package org.craftercms.engine.scripting.impl;

import org.apache.commons.collections.MapUtils;
import org.craftercms.core.util.cache.impl.CachingAwareObjectBase;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * A JSR 233 compiled script.
 *
 * @author Alfonso Vásquez
 */
public class Jsr233CompiledScript extends CachingAwareObjectBase implements Script {

    protected CompiledScript script;
    protected Map<String, Object> globalVariables;

    public Jsr233CompiledScript(CompiledScript script, Map<String, Object> globalVariables) {
        this.script = script;
        this.globalVariables = globalVariables;
    }

    @Override
    public Object execute(Map<String, Object> variables) throws ScriptException {
        Bindings bindings = new SimpleBindings();
        if (MapUtils.isNotEmpty(globalVariables)) {
            bindings.putAll(globalVariables);
        }
        if (MapUtils.isNotEmpty(variables)) {
            bindings.putAll(variables);
        }

        try {
            return script.eval(bindings);
        } catch (Exception e) {
            throw new ScriptException(e.getMessage(), e);
        }
    }

}
