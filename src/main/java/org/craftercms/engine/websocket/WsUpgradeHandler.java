/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Upgrade websocket handler
 */
public class WsUpgradeHandler implements HttpUpgradeHandler {

    private static final Logger logger = LoggerFactory.getLogger(WsUpgradeHandler.class);

    ExecutorService exec;
    InputStream socketIn;
    OutputStream socketOut;
    Socket socket;
    Future<?> future;

    public void preInit(ExecutorService exec, InputStream socketIn, OutputStream socketOut, Socket socket) {
        this.exec = exec;
        this.socketIn = socketIn;
        this.socketOut = socketOut;
        this.socket = socket;
    }

    /**
     * Forward back and forth the websocket stream between websocket server and web client via engine as the proxy
     * @param wc the WebConnection object associated to this upgrade request
     */
    @Override
    public void init(WebConnection wc) {
        logger.debug("* Websocket| Upgrade begin");
        try {
            var servletIn = wc.getInputStream();
            var servletOut = wc.getOutputStream();
            future = exec.submit(() -> {
                logger.debug("> Websocket| Websocket server -> Engine");
                try {
                    forwardStreamData(socketIn, servletOut, true);
                } catch (IOException e) {
                    logger.error("Error while forwarding websocket stream", e);
                }

                return null;
            });

            logger.debug("> Websocket| Client -> Engine");
            forwardStreamData(servletIn, socketOut, false);

            future.get();
        } catch (Exception e) {
            logger.error("Error while forwarding websocket stream", e);
        } finally {
            if (future != null) {
                future.cancel(true);
            }
        }
    }

    @Override
    public void destroy() {
        logger.debug("* Websocket| Upgrade closing");
        if (future != null) {
            future.cancel(true);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            logger.debug("Exception while closing socket", ex);
        }
        logger.debug("* Websocket| Upgrade close");
    }

    /**
     * Forward the whole bytes from an input stream to an output stream
     * @param inputStream the input stream to read bytes from
     * @param outputStream the output stream to write bytes into
     * @param flushOutput true to flush output stream right after reading each block
     * @throws IOException if there is an error close the output stream
     */
    private void forwardStreamData(InputStream inputStream, OutputStream outputStream, boolean flushOutput) throws IOException {
        int i = 0;
        int bs;
        try {
            while ((bs = inputStream.read()) != -1) {
                outputStream.write(bs);
                if (flushOutput) {
                    outputStream.flush();
                }
                i++;
            }
        } catch (Exception exc) {
            logger.debug("> Websocket| Read/Write streams interrupted", exc);
        } finally {
            socketOut.close();
        }
        logger.debug("> Websocket| Done: {}", i);
    }
}