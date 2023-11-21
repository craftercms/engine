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
import java.io.InputStream;
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
     * Handle the initial handshake request for websocket connection and establish the websocket.
     * A WebSocket interaction begins with an HTTP request that uses the HTTP Upgrade header to upgrade to the WebSocket protocol.
     * This method first forward the handshake request to the websocket server and read the response.
     * If the response is 101, use {@link WsUpgradeHandler} to establish the websocket proxy among client <==> engine <==> websocket server.
     * @param servletRequest servlet request from client
     * @param servletResponse servlet response to client
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

        // Create a new socket connection to the websocket server
        Socket socketProxyClient = new Socket(url.getHost(), url.getPort());
        boolean closeSocket = false;
        try {
            var socketIn = socketProxyClient.getInputStream();
            var sockOut = socketProxyClient.getOutputStream();

            // Send the handshake request from engine to the websocket server
            String handshakeRequest = getProxyHandshakeRequest(servletRequest, url);
            sockOut.write(handshakeRequest.getBytes(StandardCharsets.UTF_8));
            sockOut.flush();

            // Get handshake response from websocket server to engine
            String[] responseLines = getProxyHandshakeResponse(socketIn);
            addProxyResponseHeaders(responseLines, servletResponse);
            int respCode = proxyHandshakeResponseCode(responseLines);
            if (respCode != HttpServletResponse.SC_SWITCHING_PROTOCOLS) {   // The websocket handshake failed, close the connection
                servletResponse.setStatus(respCode);
                servletResponse.flushBuffer();
                logger.debug("< Websocket| Flush");
                closeSocket = true;
            } else { // The websocket handshake succeeded, establish the websocket connection
                var upgradeHandler = servletRequest.upgrade(WsUpgradeHandler.class);
                upgradeHandler.preInit(exec, socketIn, sockOut, socketProxyClient);
            }
        } finally {
            if (closeSocket) {
                socketProxyClient.close();
            }
        }
    }

    /**
     * Get the handshake request to websocket server
     * @param servletRequest original servlet request from client
     * @param serverUrl websocket server url
     * @return GET request for handshaking websocket connection
     */
    private String getProxyHandshakeRequest(HttpServletRequest servletRequest, URL serverUrl) {
        StringBuilder req = new StringBuilder(512);
        req.append("GET " + serverUrl.getFile()).append(" HTTP/1.1");
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

        return req.toString();
    }

    /**
     * Get the websocket handshake responses lines
     * Sample of response:
     *  HTTP/1.1 101 Switching Protocols
     *  Upgrade: websocket
     *  Connection: Upgrade
     *  Sec-WebSocket-Accept: xxxxxxxxxxxxxxxxxxxxxxxxxxxx
     * @param socketIn input stream to websocket client (engine)
     * @return responses lines
     * @throws IOException
     */
    private String[] getProxyHandshakeResponse(InputStream socketIn) throws IOException {
        StringBuilder responseBytes = new StringBuilder(512);
        int b = 0;
        while (b != -1) {
            b = socketIn.read();
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

        String[] lines = responseBytes.toString().split("\r\n");
        logger.debug("< Websocket| '{}'", lines[0]);

        return lines;
    }

    /**
     * Get the handshake response code from the response lines
     * Sample of first response line: `HTTP/1.1 101 Switching Protocols`
     * This method will get the index of first and second space character and then split the status code
     * @param lines response lines
     * @return the status code (101 in case of success)
     */
    private int proxyHandshakeResponseCode(String[] lines) {
        int idx1 = lines[0].indexOf(' ');
        int idx2 = lines[0].indexOf(' ', idx1 + 1);
        int respCode = Integer.parseInt(lines[0].substring(idx1 + 1, idx2));
        return respCode;
    }

    /**
     * Add the handshake response headers to clients response headers
     * @param lines response lines
     * @param servletResponse original servlet response to client
     */
    private void addProxyResponseHeaders(String[] lines, HttpServletResponse servletResponse) {
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int index = line.indexOf(":");
            String k = line.substring(0, index);
            String headerField = line.substring(index + 2);
            logger.debug("< Websocket| {}:{}", k, headerField);
            servletResponse.setHeader(k, headerField);
        }
    }

}
