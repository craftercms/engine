/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.service.health;

import org.craftercms.engine.exception.ScriptNotFoundException;
import org.craftercms.engine.scripting.Script;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.service.context.SiteContextManager;
import org.craftercms.engine.util.GroovyScriptUtils;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link HealthCheck} implementation that runs a script in the site.
 */
public class ScriptSiteHealthCheck extends AbstractHealthCheck {

    private final boolean disableVariableRestrictions;
    private final String scriptPath;

    @ConstructorProperties({"contextManager", "scriptPath", "disableVariableRestrictions"})
    public ScriptSiteHealthCheck(final SiteContextManager contextManager, final String scriptPath, final boolean disableVariableRestrictions) {
        super(contextManager);
        this.scriptPath = scriptPath;
        this.disableVariableRestrictions = disableVariableRestrictions;
    }

    @Override
    protected boolean doCheckHealth(final String site) {
        SiteContext siteContext = contextManager.getContext(site, false);
        try {
            Script script = siteContext.getScriptFactory().getScript(scriptPath);
            Map<String, Object> scriptVariables = new HashMap<>();
            GroovyScriptUtils.addHealthCheckScriptVariables(scriptVariables, disableVariableRestrictions);
            script.execute(scriptVariables);
            return true;
        } catch (ScriptNotFoundException e) {
            logger.debug("Script '{}' not found for site '{}'", scriptPath, site);
            return true;
        } catch (Exception e) {
            logger.error("Error executing script '{}' for site '{}': '{}'", scriptPath, site, e.getMessage());
            logger.debug("Script failed with exception: ", e);
            return false;
        }
    }
}
