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
package org.craftercms.engine.http.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.craftercms.engine.exception.HttpProxyException;
import org.craftercms.engine.http.HttpProxy;
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
    private CloseableHttpClient httpClient;

    public HttpProxyImpl() {
        httpClient = HttpClientBuilder
                        .create()
                        .setConnectionManager(new PoolingHttpClientConnectionManager())
                        .build();
    }

    @Required
    public void setBaseServiceUrl(String baseServiceUrl) {
        this.baseServiceUrl = StringUtils.stripEnd(baseServiceUrl, "/");
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void proxyGet(String url, HttpServletRequest request, HttpServletResponse response) throws HttpProxyException {
        proxyRequest(url, true, request, response);
    }

    @Override
    public void proxyPost(String url, HttpServletRequest request, HttpServletResponse response) {
        proxyRequest(url, false, request, response);
    }

    protected void proxyRequest(String url, boolean isGet, HttpServletRequest request, HttpServletResponse response)
            throws HttpProxyException {
        String targetUrl = createTargetUrl(url, request);

        HttpRequestBase httpRequest = null;
        CloseableHttpResponse httpResponse = null;
        try {
            if (isGet) {
                httpRequest = createGetRequest(targetUrl, request);
            } else {
                httpRequest = createPostRequest(targetUrl, request);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Proxying to " + getRequestDescription(httpRequest));
            }

            httpResponse = httpClient.execute(httpRequest);
            response.setStatus(httpResponse.getStatusLine().getStatusCode());
            String responseBody = IOUtils.toString(httpResponse.getEntity().getContent());

            if (httpResponse.getStatusLine().getStatusCode() >= 400 && logger.isDebugEnabled()) {
                logger.debug("Received error response from " + getRequestDescription(httpRequest) + ": status = " +
                    httpResponse.getStatusLine().getReasonPhrase() + ", response body = \n" +
                    responseBody);
            }

            copyActualResponseHeaders(httpRequest, response);
            copyActualResponseBody(responseBody, response);
        } catch (Exception e) {
            String errorMsg;

            if (httpRequest != null) {
                errorMsg = "Error while proxying to " + getRequestDescription(httpRequest);
            } else {
                errorMsg = "Error while proxing to " + (isGet? "GET[" : "POST[") + targetUrl + "]";
            }

            logger.error(errorMsg, e);

            throw new HttpProxyException(errorMsg, e);
        } finally {
            if (httpRequest != null) {
                httpRequest.releaseConnection();
            }
        }
    }

    protected HttpRequestBase createGetRequest(String url, HttpServletRequest request) {
        HttpGet getRequest = new HttpGet(url);
        copyOriginalHeaders(getRequest, request);

        return getRequest;
    }

    protected HttpRequestBase createPostRequest(String url, HttpServletRequest request) throws IOException {
        HttpPost postRequest = new HttpPost(url);
        copyOriginalHeaders(postRequest, request);
        copyOriginalRequestBody(postRequest, request);

        return postRequest;
    }

    protected void copyOriginalHeaders(HttpUriRequest httpRequest, HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                String headerValue = request.getHeader(headerName);

                if (logger.isTraceEnabled()) {
                    logger.trace(getRequestDescription(httpRequest) + " copying request header " + headerName + ": " + headerValue);
                }

                httpRequest.addHeader(headerName, headerValue);
            }
        }
    }

    protected void copyOriginalRequestBody(HttpPost httpRequest, HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength > 0) {
            String contentType = request.getContentType();
            InputStream content = request.getInputStream();

            httpRequest.setEntity(new InputStreamEntity(content, contentLength, ContentType.create(contentType)));
        }
    }

    protected void copyActualResponseHeaders(HttpUriRequest httpRequest, HttpServletResponse response) {
        Header[] headers = httpRequest.getAllHeaders();
        for (Header header : headers) {
            String headerName = header.getName();
            String headerValue = header.getValue();
            
            if (!headerName.equals("Transfer-Encoding") && !header.equals("chunked")) {
                if (logger.isTraceEnabled()) {
                    logger.trace(getRequestDescription(httpRequest) + " copying response header " + headerName + ": " +  headerValue);
                }

                if (response.containsHeader(headerName)) {
                    response.setHeader(headerName, headerValue);
                } else {
                    response.addHeader(headerName, headerValue);
                }
            }
        }
    }

    protected void copyActualResponseBody(String responseBody, HttpServletResponse response) throws
        IOException {
        if (responseBody != null) {
            response.setContentLength(responseBody.length());
            OutputStream out = response.getOutputStream();
            out.write(responseBody.getBytes());
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

    private String getRequestDescription(HttpUriRequest httpRequest) {
        return httpRequest.getMethod() + "[" + httpRequest.getURI() + "]";
    }

}
