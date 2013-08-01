package org.craftercms.engine.scripting;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.craftercms.core.service.Content;
import org.craftercms.engine.exception.ScriptException;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;
import java.io.*;
import java.util.Map;

/**
 * Represents a JSR-223 script inside the Crafter content store.
 *
 * @author Alfonso Vásquez
 */
public class Script {

    protected Content script;
    protected Map<String, Object> globalScriptVariables;
    protected ScriptEngine scriptEngine;

    public Script(Content script, Map<String, Object> globalScriptVariables, ScriptEngine scriptEngine) {
        this.script = script;
        this.globalScriptVariables = globalScriptVariables;
        this.scriptEngine = scriptEngine;
    }

    public void executeScript(Map<String, Object> scriptVariables) throws ScriptException {
        InputStream scriptInput;
        try {
            scriptInput = script.getInputStream();
        } catch (Exception e) {
            throw new ScriptException("Unable to open input stream for script", e);
        }
        Reader scriptReader;
        try {
            scriptReader = new BufferedReader(new InputStreamReader(scriptInput, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen EVER
            throw new RuntimeException();
        }

        Bindings bindings = new SimpleBindings();
        if (MapUtils.isNotEmpty(globalScriptVariables)) {
            bindings.putAll(globalScriptVariables);
        }
        if (MapUtils.isNotEmpty(scriptVariables)) {
            bindings.putAll(scriptVariables);
        }

        try {
            scriptEngine.eval(scriptReader, bindings);
        } catch (Exception e) {
            throw new ScriptException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(scriptReader);
        }
    }

}
