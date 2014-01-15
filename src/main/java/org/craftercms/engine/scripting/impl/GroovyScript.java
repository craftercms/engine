package org.craftercms.engine.scripting.impl;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.collections.MapUtils;
import org.craftercms.core.util.cache.impl.CachingAwareObjectBase;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;

import java.util.HashMap;
import java.util.Map;

/**
 * Runs a script through the {@link groovy.util.GroovyScriptEngine}.
 *
 * @author Alfonso Vásquez
 */
public class GroovyScript extends CachingAwareObjectBase implements Script {

    protected GroovyScriptEngine scriptEngine;
    protected String scriptName;
    protected Map<String, Object> globalVariables;

    public GroovyScript(GroovyScriptEngine scriptEngine, String scriptName, Map<String, Object> globalVariables) {
        this.scriptEngine = scriptEngine;
        this.scriptName = scriptName;
        this.globalVariables = globalVariables;
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

        try {
            return scriptEngine.run(scriptName, new Binding(allVariables));
        } catch (Exception e) {
            throw new ScriptException(e.getMessage(), e);
        }
    }

}
