/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.scripting.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.IOUtils;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;


/**
 * Extension of {@link GroovyInterceptor} that uses a blacklist to validate the script sandbox
 *
 * @author joseross
 * @since 3.1.7
 */
public class ScriptSandbox extends GroovyInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(ScriptSandbox.class);

    /**
     * These rules will always be applied even if the default blacklist file is overridden
     */
    private static final List<String> DEFAULT_BLACKLIST = asList(
            "staticMethod java.lang.Runtime getRuntime",
            "staticMethod java.lang.System exit int",
            "new org.kohsuke.groovy.sandbox.impl.Checker$SuperConstructorWrapper java.lang.Object[]",
            "new org.kohsuke.groovy.sandbox.impl.Checker$ThisConstructorWrapper java.lang.Object[]",
            "setProperty org.craftercms.engine.scripting.impl.ScriptSandbox blacklist",
            "getProperty org.craftercms.engine.scripting.impl.ScriptSandbox blacklist",
            "method org.craftercms.engine.scripting.impl.ScriptSandbox unregister",
            "getProperty org.craftercms.engine.service.context.SiteContext scriptSandbox",
            "setProperty org.craftercms.engine.service.context.SiteContext scriptSandbox"
    );

    public static final String TYPE_METHOD = "method";
    public static final String TYPE_STATIC_METHOD = "staticMethod";
    public static final String TYPE_CONSTRUCTOR = "new";
    public static final String TYPE_SET_PROPERTY = "setProperty";
    public static final String TYPE_GET_PROPERTY = "getProperty";
    public static final String IGNORE_LINE = "#";

    /**
     * The list of rules to apply
     */
    protected List<String> blacklist;

    public ScriptSandbox(Resource resource) {
        if (resource != null && resource.exists()) {
            try (InputStream is = resource.getInputStream()) {
                blacklist = IOUtils.readLines(is)
                        .stream()
                        .filter(l -> StringUtils.isNotEmpty(l) && !StringUtils.startsWith(l, IGNORE_LINE))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                logger.error("Error reading the sandbox blacklist at {}", resource, e);
            }
        }
    }

    protected String getSignature(String type, Object receiver, String member, Object... args) {
        StringBuilder sb = new StringBuilder()
                .append(type)
                .append(StringUtils.SPACE);
        if (receiver instanceof Class<?>) {
            sb.append(((Class<?>) receiver).getCanonicalName());
        } else {
            sb.append(receiver.getClass().getCanonicalName());
        }
        if (StringUtils.isNotEmpty(member)) {
            sb.append(StringUtils.SPACE).append(member);
        }
        if (ArrayUtils.isNotEmpty(args)) {
            String argType;
            for (Object arg : args) {
                Class<?> clazz = arg.getClass();
                try {
                    Field typeField = clazz.getField("TYPE");
                    clazz = (Class<?>) typeField.get(arg);
                    argType = clazz.getSimpleName();
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // not a primitive class
                    argType = clazz.getCanonicalName();
                }
                sb.append(StringUtils.SPACE).append(argType);
            }
        }
        return sb.toString();
    }

    protected void check(String signature) {
        logger.debug("Checking the blacklist for {}", signature);

        if (DEFAULT_BLACKLIST.contains(signature)
                || (CollectionUtils.isNotEmpty(blacklist) && blacklist.contains(signature))) {
            throw new UnsupportedOperationException("Insecure call to '" + signature +
                    "', you can tweak the security sandbox per site or globally. Read more about this in the" +
                    " documentation.");
        }
    }

    @Override
    public Object onMethodCall(Invoker invoker, Object receiver, String method, Object... args) throws Throwable {
        check(getSignature(TYPE_METHOD, receiver, method, args));
        return super.onMethodCall(invoker, receiver, method, args);
    }

    @Override
    public Object onStaticCall(Invoker invoker, Class receiver, String method, Object... args) throws Throwable {
        check(getSignature(TYPE_STATIC_METHOD, receiver, method, args));
        return super.onStaticCall(invoker, receiver, method, args);
    }

    @Override
    public Object onNewInstance(Invoker invoker, Class receiver, Object... args) throws Throwable {
        check(getSignature(TYPE_CONSTRUCTOR, receiver, null, args));
        return super.onNewInstance(invoker, receiver, args);
    }

    @Override
    public Object onGetProperty(Invoker invoker, Object receiver, String property) throws Throwable {
        check(getSignature(TYPE_GET_PROPERTY, receiver, property));
        return super.onGetProperty(invoker, receiver, property);
    }

    @Override
    public Object onSetProperty(Invoker invoker, Object receiver, String property, Object value) throws Throwable {
        check(getSignature(TYPE_SET_PROPERTY, receiver, property));
        return super.onSetProperty(invoker, receiver, property, value);
    }
    
}
