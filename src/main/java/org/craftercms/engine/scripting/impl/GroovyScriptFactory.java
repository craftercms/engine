package org.craftercms.engine.scripting.impl;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;

import java.util.Map;

/**
 * {@link org.craftercms.engine.scripting.ScriptFactory} used specifically for Groovy. Very useful when scripts
 * have dependencies to other scripts.
 *
 * @author Alfonso Vásquez
 */
public class GroovyScriptFactory implements ScriptFactory {

    private static final Log logger = LogFactory.getLog(GroovyScriptFactory.class);

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
