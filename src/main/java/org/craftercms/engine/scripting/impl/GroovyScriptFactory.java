package org.craftercms.engine.scripting.impl;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceConnector;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import static org.craftercms.engine.util.groovy.SandboxedGroovyClassLoader.SITE_CODE_BASE_FORMAT;

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

        initializeSanboxedScriptEngine();
    }

    public GroovyScriptFactory(ResourceConnector resourceConnector, ClassLoader parentClassLoader,
                               Map<String, Object> globalVariables) {
        this(resourceConnector, parentClassLoader, globalVariables, false);
    }

    public GroovyScriptFactory(ResourceConnector resourceConnector, ClassLoader parentClassLoader,
                               Map<String, Object> globalVariables, boolean groovySandboxEnabled) {
        this.scriptEngine = new GroovyScriptEngine(resourceConnector, parentClassLoader);
        this.globalVariables = globalVariables;

        if (groovySandboxEnabled) {
            initializeSanboxedScriptEngine();
        }
    }

    @Override
    public String getScriptFileExtension() {
        return GROOVY_FILE_EXTENSION;
    }

    @Override
    public Script getScript(String url) throws ScriptException {
        return new GroovyScript(scriptEngine, url, globalVariables);
    }

    protected void initializeSanboxedScriptEngine() {
        GroovyClassLoader classLoader = scriptEngine.getGroovyClassLoader();
        GroovyClassLoader classLoaderWrapper = new SandboxedScriptLoaderWrapper(classLoader);

        try {
            FieldUtils.writeField(scriptEngine, "groovyLoader", classLoaderWrapper, true);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set groovyLoader field for sandboxing", e);
        }

        logger.info("Groovy script factory initialized for sandboxing");
    }

    protected class SandboxedScriptLoaderWrapper extends GroovyClassLoader {

        private GroovyClassLoader wrappedLoader;

        public SandboxedScriptLoaderWrapper(GroovyClassLoader wrappedLoader) {
            this.wrappedLoader = wrappedLoader;
        }

        @Override
        public Class parseClass(String text, String fileName) throws CompilationFailedException {
            SiteContext context = SiteContext.getCurrent();
            if (context != null) {
                String codeBase = String.format(SITE_CODE_BASE_FORMAT, context.getSiteName());
                GroovyCodeSource gcs = AccessController.doPrivileged((PrivilegedAction<GroovyCodeSource>) () ->
                        new GroovyCodeSource(text, fileName, codeBase));
                gcs.setCachable(false);

                return wrappedLoader.parseClass(gcs);
            } else {
                return wrappedLoader.parseClass(text, fileName);
            }
        }
    }

}
