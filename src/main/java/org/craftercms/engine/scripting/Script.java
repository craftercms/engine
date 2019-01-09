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
package org.craftercms.engine.scripting;

import java.util.Map;

import org.craftercms.core.util.cache.CachingAwareObject;
import org.craftercms.engine.exception.ScriptException;

/**
 * Simple interface for scripts in some non-Java language (Groovy, Jython, Javascript, etc) that can be retrieved from
 * the Crafter content store and executed inside the JVM.
 *
 * @author Alfonso Vásquez
 */
public interface Script extends CachingAwareObject {

    String getUrl();

    Object execute(Map<String, Object> variables) throws ScriptException;

}
