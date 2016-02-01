package org.craftercms.engine.scripting.impl;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import org.apache.commons.collections.MapUtils;
import org.craftercms.core.util.cache.impl.CachingAwareObjectBase;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.scripting.Script;
import org.slf4j.MDC;

/**
 * Runs a script through the {@link groovy.util.GroovyScriptEngine}.
 *
 * @author Alfonso Vásquez
 */
public class GroovyScript extends CachingAwareObjectBase implements Script {

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
