/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.lang.Callback;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.core.util.cache.CachingAwareObject;
import org.craftercms.engine.exception.ScriptException;
import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link ScriptFactory} for JSR 233 compiled scripts
 *
 * @author Alfonso Vásquez
 */
public class Jsr233CompiledScriptFactory implements ScriptFactory {

    private static final Log logger = LogFactory.getLog(Jsr233CompiledScriptFactory.class);

    public static final String DEFAULT_SCRIPT_ENGINE_NAME = "groovy";
    public static final String DEFAULT_SCRIPT_FILE_EXTENSION = "groovy";

    protected CacheTemplate cacheTemplate;
    protected ContentStoreService storeService;
    protected String scriptEngineName;
    protected String scriptFileExtension;
    protected Map<String, Object> globalVariables;
    protected Compilable scriptEngine;

    public Jsr233CompiledScriptFactory() {
        scriptEngineName = DEFAULT_SCRIPT_ENGINE_NAME;
        scriptFileExtension = DEFAULT_SCRIPT_FILE_EXTENSION;
    }

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Required
    public void setStoreService(ContentStoreService storeService) {
        this.storeService = storeService;
    }

    public void setScriptEngineName(String scriptEngineName) {
        this.scriptEngineName = scriptEngineName;
    }

    public void setScriptFileExtension(String scriptFileExtension) {
        this.scriptFileExtension = scriptFileExtension;
    }

    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    @PostConstruct
    public void init() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngine = (Compilable) scriptEngineManager.getEngineByName(scriptEngineName);
    }

    @Override
    public String getScriptFileExtension() {
        return scriptFileExtension;
    }

    @Override
    public Script getScript(String url) throws ScriptException {
        SiteContext siteContext = SiteContext.getCurrent();

        return getCachedScript(siteContext, url);
    }

    protected Script getCachedScript(final SiteContext siteContext, final String url) throws ScriptException {
        return cacheTemplate.getObject(siteContext.getContext(), new Callback<Script>() {

            @Override
            public Script execute() throws ScriptException {
                Content scriptContent;

                try {
                    scriptContent = storeService.getContent(siteContext.getContext(), url);
                } catch (PathNotFoundException e) {
                    throw new ScriptNotFoundException(
                        "No script found at " + getScriptLocation() + " in content store", e);
                } catch (Exception e) {
                    throw new ScriptException(
                        "Error while retrieving script at " + getScriptLocation() + " in content store", e);
                }

                InputStream scriptInput;
                try {
                    scriptInput = scriptContent.getInputStream();
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

                CompiledScript compiledScript;
                try {
                    compiledScript = scriptEngine.compile(scriptReader);
                } catch (Exception e) {
                    throw new ScriptException("Error while compiling script at " + getScriptLocation(), e);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Compiled script at " + getScriptLocation());
                }

                Jsr233CompiledScript script = new Jsr233CompiledScript(compiledScript, globalVariables);
                if (scriptContent instanceof CachingAwareObject) {
                    script.addDependencyKey(((CachingAwareObject)scriptContent).getKey());
                }

                return script;
            }

            private String getScriptLocation() {
                return "[site=" + siteContext.getSiteName() + ", url=" + url + "]";
            }

        }, url, "crafter.script");
    }

}
