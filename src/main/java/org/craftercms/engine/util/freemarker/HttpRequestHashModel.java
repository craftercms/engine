/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.engine.util.freemarker;

import java.util.ArrayList;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import static org.apache.commons.lang3.StringUtils.startsWith;

/**
 * Just like {@link freemarker.ext.servlet.HttpRequestHashModel}, but besides returning request attributes, it also
 * returns values for the following properties of the request:
 *
 * <ul>
 *     <li>scheme</li>
 *     <li>serverName</li>
 *     <li>serverPort</li>
 *     <li>contextPath</li>
 *     <li>servletPath</li>
 *     <li>requestURI</li>
 *     <li>queryString</li>
 * </ul>
 *
 * @author avasquez
 */
public class HttpRequestHashModel implements TemplateHashModelEx {

    public static final String KEY_SCHEME = "scheme";
    public static final String KEY_SERVER_NAME = "serverName";
    public static final String KEY_SERVER_PORT = "serverPort";
    public static final String KEY_CONTEXT_PATH = "contextPath";
    public static final String KEY_SERVLET_PATH = "servletPath";
    public static final String KEY_REQUEST_URI = "requestURI";
    public static final String KEY_QUERY_STRING = "queryString";

    public static final String NAMESPACE_SPRING = "org.springframework";

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ObjectWrapper wrapper;
    private final boolean disableRestrictions;

    public HttpRequestHashModel(HttpServletRequest request, HttpServletResponse response, ObjectWrapper wrapper,
                                boolean disableRestrictions) {
        this.request = request;
        this.response = response;
        this.wrapper = wrapper;
        this.disableRestrictions = disableRestrictions;
    }

    public TemplateModel get(String key) throws TemplateModelException {
        if (!disableRestrictions && startsWith(key, NAMESPACE_SPRING)) {
            return null;
        }
        switch (key) {
            case KEY_SCHEME:
                return wrapper.wrap(request.getScheme());
            case KEY_SERVER_NAME:
                return wrapper.wrap(request.getServerName());
            case KEY_SERVER_PORT:
                return wrapper.wrap(request.getServerPort());
            case KEY_CONTEXT_PATH:
                return wrapper.wrap(request.getContextPath());
            case KEY_SERVLET_PATH:
                return wrapper.wrap(request.getServletPath());
            case KEY_REQUEST_URI:
                return wrapper.wrap(request.getRequestURI());
            case KEY_QUERY_STRING:
                return wrapper.wrap(request.getQueryString());
            default:
                return wrapper.wrap(request.getAttribute(key));
        }
    }

    public boolean isEmpty()
    {
        return !request.getAttributeNames().hasMoreElements();
    }

    public int size() {
        int result = 0;

        for (Enumeration<String> enumeration = request.getAttributeNames(); enumeration.hasMoreElements();) {
            enumeration.nextElement();
            ++result;
        }

        return result;
    }

    public TemplateCollectionModel keys() {
        ArrayList<String> keys = new ArrayList<>();

        for (Enumeration<String> enumeration = request.getAttributeNames(); enumeration.hasMoreElements();) {
            keys.add(enumeration.nextElement());
        }

        return new SimpleCollection(keys.iterator(), wrapper);
    }

    public TemplateCollectionModel values() {
        ArrayList<Object> values = new ArrayList<>();

        for (Enumeration<String> enumeration = request.getAttributeNames(); enumeration.hasMoreElements();) {
            values.add(request.getAttribute(enumeration.nextElement()));
        }

        return new SimpleCollection(values.iterator(), wrapper);
    }

    public HttpServletRequest getRequest()
    {
        return request;
    }

    public HttpServletResponse getResponse()
    {
        return response;
    }

    public ObjectWrapper getObjectWrapper()
    {
        return wrapper;
    }

}
