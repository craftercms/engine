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
package org.craftercms.engine.util.servlet;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Extension of {@link ProxyServlet} that uses the current site configuration
 *
 * @author joseross
 * @since 3.1.7
 */
public class ConfigAwareProxyServlet extends ProxyServlet {

    // Expose protected constants
    public static final String ATTR_TARGET_URI = ProxyServlet.ATTR_TARGET_URI;
    public static final String ATTR_TARGET_HOST = ProxyServlet.ATTR_TARGET_HOST;

    @Override
    protected void initTarget() {
        // Do nothing ... the target url will be resolved for each request
    }

    @Override
    protected HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri, HttpServletRequest request)
            throws IOException {
        // Check if the request was cached
        if (request instanceof ContentCachingRequestWrapper) {
            // Use the cached content instead of the input stream
            HttpEntityEnclosingRequest proxyRequest =
                    new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
            byte[] cachedContent = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
            // If the cache is empty, the input stream has not been consumed
            if (cachedContent.length != 0) {
                proxyRequest.setEntity(
                        new InputStreamEntity(new ByteArrayInputStream(cachedContent), cachedContent.length));
                return proxyRequest;
            }
        }

        // Use the input stream as usual
        return super.newProxyRequestWithEntity(method, proxyRequestUri, request);
    }

    /**
     * Override how ProxyServlet copy response headers.
     * If a header is added via HttpProxyFilter, do not add it again
     * @param proxyResponse
     * @param servletRequest
     * @param servletResponse
     */
    @Override
    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            if (!servletResponse.containsHeader(header.getName())) {
                copyResponseHeader(servletRequest, servletResponse, header);
            }
        }
    }

}
