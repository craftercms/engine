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
import org.craftercms.engine.websocket.WsUpgradeHandler;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Extension of {@link ProxyServlet} that uses the current site configuration
 *
 * @author joseross
 * @since 3.1.7
 */
public class ConfigAwareProxyServlet extends ProxyServlet {

    private static final Logger logger = LoggerFactory.getLogger(ConfigAwareProxyServlet.class);

    // Expose protected constants
    public static final String ATTR_TARGET_URI = ProxyServlet.ATTR_TARGET_URI;
    public static final String ATTR_TARGET_HOST = ProxyServlet.ATTR_TARGET_HOST;
    public static final String WEBSOCKET_IDENTIFIER_HEADER = "Sec-WebSocket-Key";

    protected ExecutorService exec;

    @Override
    public void init() throws ServletException {
        super.init();
        exec = Executors.newCachedThreadPool();
    }

    @Override
    protected void initTarget() {
        // Do nothing ... the target url will be resolved for each request
    }

    @Override
    public void destroy() {
        super.destroy();
        exec.shutdown();
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

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws IOException, ServletException {
        var websocketKey = servletRequest.getHeader(WEBSOCKET_IDENTIFIER_HEADER);
        if (websocketKey != null) {
            websocketHandlerService(servletRequest, servletResponse);
        } else {
            super.service(servletRequest, servletResponse);
        }
    }

    /**
     * Handle a websocket request by upgrading requests with 101 responses
     * @param servletRequest request
     * @param servletResponse response
     */
    private void websocketHandlerService(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
        //initialize request attributes from caches if unset by a subclass by this point
        if (servletRequest.getAttribute(ATTR_TARGET_URI) == null) {
            servletRequest.setAttribute(ATTR_TARGET_URI, targetUri);
        }
        if (servletRequest.getAttribute(ATTR_TARGET_HOST) == null) {
            servletRequest.setAttribute(ATTR_TARGET_HOST, targetHost);
        }
        String proxyRequestUri = rewriteUrlFromRequest(servletRequest);
        URL url = new URL(proxyRequestUri);

        Socket sock = new Socket(url.getHost(), url.getPort());
        boolean closeSocket = false;
        try {
            var sockIn = sock.getInputStream();
            var sockOut = sock.getOutputStream();

            StringBuilder req = new StringBuilder(512);
            req.append("GET " + url.getFile()).append(" HTTP/1.1");
            logger.debug("> Websocket|", req);
            req.append("\r\n");
            var headerNames = servletRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                var name = headerNames.nextElement();
                String header = servletRequest.getHeader(name);
                logger.debug("> Websocket| {}:{}", name, header);
                req.append(name + ": " + header + "\r\n");
            }
            req.append("\r\n");

            sockOut.write(req.toString().getBytes(StandardCharsets.UTF_8));
            sockOut.flush();

            StringBuilder responseBytes = new StringBuilder(512);
            int b = 0;
            while (b != -1) {
                b = sockIn.read();
                if (b != -1) {
                    responseBytes.append((char) b);
                    var len = responseBytes.length();
                    if (len >= 4
                            && responseBytes.charAt(len - 4) == '\r'
                            && responseBytes.charAt(len - 3) == '\n'
                            && responseBytes.charAt(len - 2) == '\r'
                            && responseBytes.charAt(len - 1) == '\n'
                    ) {
                        break;
                    }
                }
            }

            String[] rows = responseBytes.toString().split("\r\n");

            String response = rows[0];
            logger.debug("< Websocket| '{}'", response);

            int idx1 = response.indexOf(' ');
            int idx2 = response.indexOf(' ', idx1 + 1);

            for (int i = 1; i < rows.length; i++) {
                String line = rows[i];
                int idx3 = line.indexOf(":");
                String k = line.substring(0, idx3);
                String headerField = line.substring(idx3 + 2);
                logger.debug("< Websocket| {}:{}", k, headerField);
                servletResponse.setHeader(k, headerField);
            }

            int respCode = Integer.parseInt(response.substring(idx1 + 1, idx2));
            if (respCode != HttpServletResponse.SC_SWITCHING_PROTOCOLS) {
                servletResponse.setStatus(respCode);
                servletResponse.flushBuffer();
                logger.debug("< Websocket| Flush");
                closeSocket = true;
            } else {
                var upgradeHandler = servletRequest.upgrade(WsUpgradeHandler.class);
                upgradeHandler.preInit(exec, sockIn, sockOut, sock);
            }
        } finally {
            if (closeSocket) {
                sock.close();
            }
        }
    }

}
