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

package org.craftercms.engine.scripting.impl;

import java.util.Map;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceConnector;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;

/**
 * {@link org.craftercms.engine.scripting.ScriptFactory} used specifically for Groovy. Very useful when scripts
 * have dependencies to other scripts.
 *
 * @author Alfonso Vásquez
 */
public class GroovyScriptFactory implements ScriptFactory {

    public static final String GROOVY_FILE_EXTENSION = "groovy";

    protected GroovyScriptEngine scriptEngine;
    protected Map<String, Object> globalVariables;

    public GroovyScriptFactory(ResourceConnector resourceConnector, Map<String, Object> globalVariables) {
        this.scriptEngine = new GroovyScriptEngine(resourceConnector);
        this.globalVariables = globalVariables;
    }

    public GroovyScriptFactory(ResourceConnector resourceConnector, ClassLoader parentClassLoader,
                               Map<String, Object> globalVariables) {
        this.scriptEngine = new GroovyScriptEngine(resourceConnector, parentClassLoader);
        this.globalVariables = globalVariables;
    }

    @Override
    public String getScriptFileExtension() {
        return GROOVY_FILE_EXTENSION;
    }

    @Override
    public Script getScript(String url) throws ScriptException {
        return new GroovyScript(scriptEngine, url, globalVariables);
    }

}
