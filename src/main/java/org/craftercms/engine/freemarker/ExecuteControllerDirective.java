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
package org.craftercms.engine.freemarker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.model.SiteItem;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.scripting.ScriptFactory;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.GroovyScriptUtils;
import org.craftercms.engine.view.CrafterPageView;
import org.springframework.beans.factory.annotation.Required;

/**
 * Freemarker directive that allows to execute scripts/controllers from inside Freemarker templates. The directive receives a single
 * parameter that is that path of the controller in the site.
 *
 * @author avasquez
 */
public class ExecuteControllerDirective implements TemplateDirectiveModel {

    private static final Log logger = LogFactory.getLog(ExecuteControllerDirective.class);

    public static final String PATH_PARAM_NAME = "path";

    protected ServletContext servletContext;

    @Required
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {
        TemplateModel pathParam = (TemplateModel) params.get(PATH_PARAM_NAME);
        if (pathParam == null) {
            throw new IllegalArgumentException("No '" + PATH_PARAM_NAME + "' param specified");
        } else {
            executeController(getPath(pathParam, env), env);
        }
    }

    protected void executeController(String path, Environment env) throws TemplateException {
        Map<String, Object> scriptVariables = createScriptVariables(env);
        SiteContext siteContext = SiteContext.getCurrent();

        if (siteContext != null) {
            ScriptFactory scriptFactory = siteContext.getScriptFactory();

            if (scriptFactory == null) {
                throw new IllegalStateException("No script factory associate to current site context '" +
                                                siteContext.getSiteName() + "'");
            }

            Script script;
            try {
                script = scriptFactory.getScript(path);
            } catch (Exception e) {
                throw new TemplateException("Unable to load controller at '" + path + "'", e, env);
            }

            executeController(script, scriptVariables, env);
        } else {
            throw new IllegalStateException("No current site context found");
        }
    }

    protected void executeController(Script script, Map<String, Object> variables, Environment env) throws TemplateException {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing controller at " + script.getUrl());
        }

        try {
            script.execute(variables);
        } catch (Exception e) {
            throw new TemplateException("Error executing controller at " + script.getUrl(), e, env);
        }
    }

    protected Map<String, Object> createScriptVariables(Environment env) throws TemplateException {
        Map<String, Object> variables = new HashMap<String, Object>();
        RequestContext context = RequestContext.getCurrent();
        SiteItem contentModel = getContentModel(env);
        Object templateModel = getTemplateModel(env);

        if (context != null) {
            GroovyScriptUtils.addSiteItemScriptVariables(variables, context.getRequest(), context.getResponse(),
                                                         servletContext, contentModel, templateModel);
        } else {
            throw new IllegalStateException("No current request context found");
        }

        return variables;
    }

    protected String getPath(TemplateModel pathParam, Environment env) throws TemplateException {
        Object unwrappedPath = DeepUnwrap.unwrap(pathParam);
        if (unwrappedPath instanceof String) {
            return (String)unwrappedPath;
        } else {
            throw new TemplateException("Param '" + PATH_PARAM_NAME + " of unexpected type: expected: " + String.class.getName() +
                                        ", actual: " + unwrappedPath.getClass().getName(), env);
        }
    }

    protected SiteItem getContentModel(Environment env) throws TemplateException {
        TemplateModel contentModel = env.getVariable(CrafterPageView.KEY_CONTENT_MODEL);
        if (contentModel != null) {
            Object unwrappedContentModel = DeepUnwrap.unwrap(contentModel);
            if (unwrappedContentModel instanceof SiteItem) {
                return (SiteItem)unwrappedContentModel;
            } else {
                throw new TemplateException("Variable '" + CrafterPageView.KEY_CONTENT_MODEL + " of unexpected type: expected: " +
                                            SiteItem.class.getName() + ", actual: " + unwrappedContentModel.getClass().getName(),
                                            env);
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Object getTemplateModel(Environment env) throws TemplateException {
        return new EnvironmentGroovyBeanWrapper(env);
    }

    public static class EnvironmentGroovyBeanWrapper {

        protected Environment env;

        public EnvironmentGroovyBeanWrapper(Environment env) {
            this.env = env;
        }

        public Object get(String varName) throws TemplateModelException {
            TemplateModel var = env.getVariable(varName);
            if (var != null) {
                return DeepUnwrap.unwrap(var);
            } else {
                return null;
            }
        }

        public void set(String varName, Object varValue) throws TemplateModelException {
            env.setVariable(varName, env.getObjectWrapper().wrap(varValue));
        }

    }


}
