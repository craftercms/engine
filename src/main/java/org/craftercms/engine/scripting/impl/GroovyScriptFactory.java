package org.craftercms.engine.scripting.impl;

import groovy.util.GroovyScriptEngine;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 * {@link org.craftercms.engine.scripting.ScriptFactory} used specifically for Groovy. Very useful when scripts
 * have dependencies to other scripts.
 *
 * @author Alfonso Vásquez
 */
public class GroovyScriptFactory implements ScriptFactory {

    public static final String GROOVY_SCRIPT_EXTENSION = "groovy";

    protected GroovyScriptEngine scriptEngine;
    protected Map<String, Object> globalVariables;

    @Required
    public void setResourceConnector(ContentResourceConnector resourceConnector) {
        scriptEngine = new GroovyScriptEngine(resourceConnector);
    }

    @Required
    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    @Override
    public String getScriptFileExtension() {
        return GROOVY_SCRIPT_EXTENSION;
    }

    @Override
    public Script getScript(String url) throws ScriptException {
        return new GroovyScript(scriptEngine, url, globalVariables);
    }

}
