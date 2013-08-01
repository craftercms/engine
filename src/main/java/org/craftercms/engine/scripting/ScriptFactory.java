package org.craftercms.engine.scripting;

import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.servlet.filter.AbstractSiteContextResolvingFilter;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;

/**
 * Returns a {@link Script} for a given url inside the Crafter content store.
 *
 * @author Alfonso Vásquez
 */
public class ScriptFactory {

    public static final String DEFAULT_SCRIPT_ENGINE_NAME = "JavaScript";

    protected ContentStoreService storeService;
    protected String scriptEngineName;
    protected Map<String, Object> globalScriptVariables;
    protected ScriptEngine scriptEngine;

    public ScriptFactory() {
        scriptEngineName = DEFAULT_SCRIPT_ENGINE_NAME;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    public void setScriptEngineName(String scriptEngineName) {
        this.scriptEngineName = scriptEngineName;
    }

    public void setGlobalScriptVariables(Map<String, Object> globalScriptVariables) {
        this.globalScriptVariables = globalScriptVariables;
    }

    @PostConstruct
    public void init() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName(scriptEngineName);
    }

    public Script getScript(String url) throws ScriptException {
        Context context = AbstractSiteContextResolvingFilter.getCurrentContext().getContext();
        Content script;

        try {
            script = storeService.getContent(context, url);
        } catch (PathNotFoundException e) {
            throw new ScriptNotFoundException("No script found at " + url + " in content store", e);
        } catch (Exception e) {
            throw new ScriptException("Unable to retrieve script at " + url + " in content store", e);
        }

        return new Script(script, globalScriptVariables, scriptEngine);
    }

}
