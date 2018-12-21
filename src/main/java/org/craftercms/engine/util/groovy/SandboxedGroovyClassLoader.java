/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.control.CompilationFailedException;
import org.craftercms.engine.service.context.SiteContext;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Extension of {@code GroovyClassLoader} that overwrites {@code parseClass} to provide a custom Crafter code base
 * that can be used for sandboxing the Groovy code of each site. See the Servlet container's Java policy (e.g.
 * catalina.policy) for examples on how to do site sandboxing.
 *
 * @author avasquez
 */
public class SandboxedGroovyClassLoader extends GroovyClassLoader {

    public static final String SITE_CODE_BASE_FORMAT = "/craftercms/scripts/%s";

    public SandboxedGroovyClassLoader(ClassLoader loader) {
        super(loader);
    }

    @Override
    public Class parseClass(String text, String fileName) throws CompilationFailedException {
        SiteContext context = SiteContext.getCurrent();
        if (context != null) {
            String codeBase = String.format(SITE_CODE_BASE_FORMAT, context.getSiteName());
            GroovyCodeSource gcs = AccessController.doPrivileged((PrivilegedAction<GroovyCodeSource>) () ->
                    new GroovyCodeSource(text, fileName, codeBase));
            gcs.setCachable(false);

            return parseClass(gcs);
        } else {
            return super.parseClass(text, fileName);
        }
    }

}
