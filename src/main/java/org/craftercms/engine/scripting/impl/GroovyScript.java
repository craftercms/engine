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

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import org.apache.commons.collections.MapUtils;
import org.craftercms.core.util.cache.impl.AbstractCachingAwareObject;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.scripting.Script;
import org.slf4j.MDC;

/**
 * Runs a script through the {@link groovy.util.GroovyScriptEngine}.
 *
 * @author Alfonso Vásquez
 */
public class GroovyScript extends AbstractCachingAwareObject implements Script {

    private static final String SCRIPT_URL_MDC_KEY = "scriptUrl";

    protected GroovyScriptEngine scriptEngine;
    protected String scriptUrl;
    protected Map<String, Object> globalVariables;

    public GroovyScript(GroovyScriptEngine scriptEngine, String scriptUrl, Map<String, Object> globalVariables) {
        this.scriptEngine = scriptEngine;
        this.scriptUrl = scriptUrl;
        this.globalVariables = globalVariables;
    }

    @Override
    public String getUrl() {
        return scriptUrl;
    }

    @Override
    public Object execute(Map<String, Object> variables) throws ScriptException {
        Map<String, Object> allVariables = new HashMap<String, Object>();

        if (MapUtils.isNotEmpty(globalVariables)) {
            allVariables.putAll(globalVariables);
        }
        if (MapUtils.isNotEmpty(variables)) {
            allVariables.putAll(variables);
        }

        MDC.put(SCRIPT_URL_MDC_KEY, scriptUrl);

        try  {
            return scriptEngine.run(scriptUrl, new Binding(allVariables));
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (e instanceof ResourceException && cause instanceof FileNotFoundException) {
                throw new ScriptNotFoundException(cause.getMessage(), cause);
            } else {
                throw new ScriptException(e.getMessage(), e);
            }
        } finally {
            MDC.remove(SCRIPT_URL_MDC_KEY);
        }
    }

    @Override
    public String toString() {
        return "GroovyScript{" +
            "scriptUrl='" + scriptUrl + '\'' +
            '}';
    }

}
