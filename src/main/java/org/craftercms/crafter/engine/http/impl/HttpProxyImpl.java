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
package org.craftercms.crafter.engine.http.impl;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.crafter.engine.exception.HttpProxyException;
import org.craftercms.crafter.engine.http.HttpProxy;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * Default proxy implementation that proxies {@code baseUrl/url} through a REST call.
 *
 * @author Alfonso Vásquez
 */
public class HttpProxyImpl implements HttpProxy {

    private static final Log logger = LogFactory.getLog(HttpProxyImpl.class);

    private String baseServiceUrl;
    private HttpClient httpClient;

    public HttpProxyImpl() {
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
    }

    @Required
    public void setBaseServiceUrl(String baseServiceUrl) {
        this.baseServiceUrl = StringUtils.stripEnd(baseServiceUrl, "/");
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void proxyGet(String url, HttpServletRequest request, HttpServletResponse response) throws HttpProxyException {
        proxyMethod(url, true, request, response);
    }

    @Override
    public void proxyPost(String url, HttpServletRequest request, HttpServletResponse response) {
        proxyMethod(url, false, request, response);
    }

    protected void proxyMethod(String url, boolean isGet, HttpServletRequest request, HttpServletResponse response)
            throws HttpProxyException {
        url = createTargetUrl(url, request);

        HttpMethod httpMethod = null;
        try {
            if (isGet) {
                httpMethod = createGetMethod(url, request);
            } else {
                httpMethod = createPostMethod(url, request);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Proxying to " + getMethodDescription(httpMethod));
            }

            int status = httpClient.executeMethod(httpMethod);
            response.setStatus(status);

            if (status >= 400 && logger.isDebugEnabled()) {
                logger.debug("Received error response from " + getMethodDescription(httpMethod) + ": status = " + httpMethod
                        .getStatusText() + ", response body = \n" + httpMethod.getResponseBodyAsString());
            }

            copyMethodResponseHeadersToResponse(httpMethod, response);
            copyMethodResponseBodyToResponse(httpMethod, response);
        } catch (Exception e) {
            String errorMsg;

            if (httpMethod != null) {
                errorMsg = "Error while proxying to " + getMethodDescription(httpMethod);
            } else {
                errorMsg = "Error while proxing to " + (isGet? "GET[" : "POST[") + url + "]";
            }

            logger.error(errorMsg, e);

            throw new HttpProxyException(errorMsg, e);
        } finally {
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }
    }

    protected HttpMethod createGetMethod(String url, HttpServletRequest request) {
        GetMethod getMethod = new GetMethod(url);
        copyRequestHeadersToMethod(getMethod, request);

        return getMethod;
    }

    protected HttpMethod createPostMethod(String url, HttpServletRequest request) throws IOException {
        PostMethod postMethod = new PostMethod(url);
        copyRequestHeadersToMethod(postMethod, request);
        copyRequestBodyToMethod(postMethod, request);

        return postMethod;
    }

    protected void copyRequestHeadersToMethod(HttpMethod httpMethod, HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                String headerValue = request.getHeader(headerName);

                if (logger.isTraceEnabled()) {
                    logger.trace(getMethodDescription(httpMethod) + " copying request header " + headerName + ": " + headerValue);
                }

                httpMethod.addRequestHeader(headerName, headerValue);
            }
        }
    }

    protected void copyRequestBodyToMethod(PostMethod httpMethod, HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength > 0) {
            String contentType = request.getContentType();
            InputStream content = request.getInputStream();

            httpMethod.setRequestEntity(new InputStreamRequestEntity(content, contentLength, contentType));
        }
    }

    protected void copyMethodResponseHeadersToResponse(HttpMethod httpMethod, HttpServletResponse response) {
        Header[] headers = httpMethod.getResponseHeaders();
        for (Header header : headers) {
            String headerName = header.getName();
            String headerValue = header.getValue();
            
            if (!headerName.equals("Transfer-Encoding") && !header.equals("chunked")) {
                if (logger.isTraceEnabled()) {
                    logger.trace(getMethodDescription(httpMethod) + " copying response header " + headerName + ": " +  headerValue);
                }

                if (response.containsHeader(headerName)) {
                    response.setHeader(headerName, headerValue);
                } else {
                    response.addHeader(headerName, headerValue);
                }
            }
        }
    }

    protected void copyMethodResponseBodyToResponse(HttpMethod httpMethod, HttpServletResponse response) throws IOException {
        byte[] responseBody = httpMethod.getResponseBody();
        if (responseBody != null) {
            response.setContentLength(responseBody.length);

            OutputStream out = response.getOutputStream();
            out.write(responseBody);
            out.flush();
        }
    }

    protected String createTargetUrl(String url, HttpServletRequest request) {
        StringBuilder targetUrl = new StringBuilder();

        if (!url.startsWith(baseServiceUrl)) {
            targetUrl.append(baseServiceUrl);
            if (!baseServiceUrl.endsWith("/") && !url.startsWith("/")) {
                targetUrl.append("/");
            }
        }

        targetUrl.append(url).append(createTargetQueryString(request));

        return targetUrl.toString();
    }

    protected String createTargetQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            queryString = "";
        }

        return queryString;
    }

    private String getMethodDescription(HttpMethod httpMethod) {
        try {
            return httpMethod.getName() + "[" + httpMethod.getURI() + "]";
        } catch (URIException e) {
            return httpMethod.getName() + "[" + httpMethod.getPath() + "]";
        }
    }

}
