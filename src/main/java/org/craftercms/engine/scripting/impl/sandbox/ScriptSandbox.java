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
package org.craftercms.engine.scripting.impl.sandbox;

import com.google.common.collect.ImmutableSet;
import groovy.lang.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.IOUtils;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaMethod;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;


/**
 * Extension of {@link GroovyInterceptor} that uses a blacklist to validate the script sandbox
 *
 * Based on https://github.com/jenkinsci/script-security-plugin/blob/master/src/main/java/org/jenkinsci/plugins/scriptsecurity/sandbox/groovy/SandboxInterceptor.java
 *
 * @author joseross
 * @since 3.1.7
 */
public class ScriptSandbox extends GroovyInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(ScriptSandbox.class);

    private static final Class<?>[] DGM_CLASSES = {
            DefaultGroovyMethods.class,
            StringGroovyMethods.class,
            SwingGroovyMethods.class,
            SqlGroovyMethods.class,
            XmlGroovyMethods.class,
            EncodingGroovyMethods.class,
            DateGroovyMethods.class,
            ProcessGroovyMethods.class,
    };

    private static final Set<String> NUMBER_MATH_NAMES = ImmutableSet.of("plus", "minus", "multiply", "div",
            "compareTo", "or", "and", "xor", "intdiv", "mod", "leftShift", "rightShift", "rightShiftUnsigned");

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
            String argType = null;
            for (Object arg : args) {
                if (arg != null) {
                    Class<?> clazz = arg.getClass();
                    try {
                        Field typeField = clazz.getField("TYPE");
                        clazz = (Class<?>) typeField.get(arg);
                        argType = clazz.getSimpleName();
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // not a primitive class
                        argType = clazz.getCanonicalName();
                    }
                }
                sb.append(StringUtils.SPACE).append(argType);
            }
        }
        return sb.toString();
    }

    protected void check(String signature) {
        logger.info("Checking the blacklist for {}", signature);

        if (DEFAULT_BLACKLIST.contains(signature)
                || (CollectionUtils.isNotEmpty(blacklist) && blacklist.contains(signature))) {
            throw new UnsupportedOperationException("Insecure call to '" + signature +
                    "', you can tweak the security sandbox to allow it. Read more about this in the" +
                    " documentation.");
        }
    }

    @Override
    public Object onMethodCall(Invoker invoker, Object receiver, String method, Object... args) throws Throwable {

        if (method.equals("invokeMethod") && args.length == 2 && args[0] instanceof String) {
            if (args[1] == null) {
                onMethodCall(invoker, receiver, (String) args[0]);
            } else {
                onMethodCall(invoker, receiver, (String) args[0], args[1]);
            }
        }

        Method m = GroovyCallSiteSelector.method(receiver, method, args);
        if (m == null) {
            if (receiver instanceof Number && NUMBER_MATH_NAMES.contains(method)) {
                // Synthetic methods like Integer.plus(Integer).
                return super.onMethodCall(invoker, receiver, method, args);
            }

            // look for GDK methods
            Object[] selfArgs = new Object[args.length + 1];
            selfArgs[0] = receiver;
            System.arraycopy(args, 0, selfArgs, 1, args.length);

            for (Class<?> dgmClass : DGM_CLASSES) {
                Method dgmMethod = GroovyCallSiteSelector.staticMethod(dgmClass, method, selfArgs);
                if (dgmMethod != null) {
                    check(getSignature(TYPE_STATIC_METHOD, dgmMethod.getDeclaringClass(), dgmMethod.getName(), selfArgs));
                    return super.onMethodCall(invoker, receiver, method, args);
                }
            }

            // allow calling Closure elements of Maps as methods
            if (receiver instanceof Map) {
                Object element = onMethodCall(invoker, receiver, "get", method);
                if (element instanceof Closure) {
                    return onMethodCall(invoker, element, "call", args);
                }
            }

            // Allow calling closure variables from a script binding as methods
            if (receiver instanceof Script) {
                Script s = (Script) receiver;
                if (s.getBinding().hasVariable(method)) {
                    Object var = s.getBinding().getVariable(method);
                    if (!InvokerHelper.getMetaClass(var).respondsTo(var, "call", (Object[]) args).isEmpty()) {
                        return onMethodCall(invoker, var, "call", args);
                    }
                }
            }

            // if no matching method, look for catchAll "invokeMethod"
            try {
                receiver.getClass().getMethod("invokeMethod", String.class, Object.class);
                return onMethodCall(invoker, receiver, "invokeMethod", method, args);
            } catch (NoSuchMethodException e) {
                // fall through
            }

            MetaMethod metaMethod = findMetaMethod(receiver, method, args);
            if (metaMethod instanceof ClosureMetaMethod) {
                return super.onMethodCall(invoker, receiver, method, args);
            }

            // no such method exists
            throw new MissingMethodException(method, receiver.getClass(), args);
        }

        check(getSignature(TYPE_METHOD, m.getDeclaringClass(), m.getName(), args));
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

    private MetaMethod findMetaMethod(Object receiver, String method, Object[] args) {
        Class<?>[] types = new Class[args.length];
        for (int i = 0; i < types.length; i++) {
            Object arg = args[i];
            types[i] = arg == null ? /* is this right? */void.class : arg.getClass();
        }
        try {
            return DefaultGroovyMethods.getMetaClass(receiver).pickMethod(method, types);
        } catch (GroovyRuntimeException e) { // ambiguous call, supposedly
            logger.trace("Could not find metamethod for {} {} {}", receiver.getClass(), method, Arrays.toString(types), e);
            return null;
        }
    }

}
