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
package org.craftercms.engine.controller;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.exception.HttpStatusCodeException;
import org.craftercms.engine.http.HttpProxy;
import org.craftercms.engine.http.HttpProxyRegistry;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * All requests served by this handler will be proxied through a {@link org.craftercms.engine.http.HttpProxy},
 * determined by the {proxyName} URI template var.
 *
 * @author Alfonso Vásquez
 */
public class HttpProxyRequestHandler extends WebContentGenerator implements HttpRequestHandler {

    public static String PROXY_NAME_URI_TEMPLATE_VAR_NAME = "proxyName";

    private HttpProxyRegistry proxyRegistry;

    public HttpProxyRequestHandler() {
        super(METHOD_GET, METHOD_POST);

        setRequireSession(false);
    }

    @Required
    public void setProxyRegistry(HttpProxyRegistry proxyRegistry) {
        this.proxyRegistry = proxyRegistry;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        checkRequest(request);
        prepareResponse(response);

        String proxyName = getProxyNameUriTemplateVar(request);
        String proxyUrl = getUrlToProxy(request);

        HttpProxy proxy = proxyRegistry.get(proxyName);
        if (proxy == null) {
            throw new HttpStatusCodeException(HttpStatus.NOT_FOUND, "Proxy name '" + proxyName + "' in URL doesn't " +
                                                                    "correspond to a registered proxy");
        }

        if (METHOD_GET.equals(request.getMethod())) {
            proxy.proxyGet(proxyUrl, request, response);
        } else {
            proxy.proxyPost(proxyUrl, request, response);
        }
    }

    @SuppressWarnings("unchecked")
    protected String getProxyNameUriTemplateVar(HttpServletRequest request) {
        Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (MapUtils.isEmpty(uriTemplateVars)) {
            throw new IllegalStateException("Required request attribute '" +
                                            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE + "' is not set");
        }

        String proxyName = uriTemplateVars.get(PROXY_NAME_URI_TEMPLATE_VAR_NAME);
        if (StringUtils.isEmpty(proxyName)) {
            throw new IllegalStateException("Required URI template var '" + PROXY_NAME_URI_TEMPLATE_VAR_NAME +
                                            "' is not set");
        }

        return proxyName;
    }

    protected String getUrlToProxy(HttpServletRequest request) {
        String url = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (StringUtils.isEmpty(url)) {
            throw new IllegalStateException("Required request attribute '" +
                                            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
        }

        return url;
    }

}
