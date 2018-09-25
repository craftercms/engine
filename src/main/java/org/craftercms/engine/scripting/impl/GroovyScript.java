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

/**
 * Runs a script through the {@link groovy.util.GroovyScriptEngine}.
 *
 * @author Alfonso Vásquez
 */
public class GroovyScript extends AbstractCachingAwareObject implements Script {

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

        try {
            return scriptEngine.run(scriptUrl, new Binding(allVariables));
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (e instanceof ResourceException && cause instanceof FileNotFoundException) {
                throw new ScriptNotFoundException(cause.getMessage(), cause);
            } else {
                throw new ScriptException(e.getMessage(), e);
            }
        }
    }

    @Override
    public String toString() {
        return "GroovyScript{" +
            "scriptUrl='" + scriptUrl + '\'' +
            '}';
    }

}
