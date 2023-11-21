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
    InputStream sockIn;
    OutputStream sockOut;
    Socket sock;
    Future<?> future;

    public void preInit(ExecutorService exec, InputStream sockIn, OutputStream sockOut, Socket sock) {
        this.exec = exec;
        this.sockIn = sockIn;
        this.sockOut = sockOut;
        this.sock = sock;
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
                int i = 0;
                try {
                    int bs;
                    // Read data from Websocket server -> Engine and write to Client
                    while ((bs = sockIn.read()) != -1) {
                        servletOut.write(bs);
                        servletOut.flush();
                        i++;
                    }
                } catch (Exception exc) {
                    logger.debug("> Websocket| Connection interrupted", exc);
                } finally {
                    servletOut.close();
                }
                logger.debug("< Websocket| Done: '{}'", i);
                return null;
            });

            logger.debug("> Websocket| Client -> Engine");
            int i = 0;
            int bs;
            try {
                // Read data from Client -> Engine and write to Websocket server
                while ((bs = servletIn.read()) != -1) {
                    sockOut.write(bs);
                    i++;
                }
            } catch (Exception exc) {
                logger.debug("> Websocket| Connection interrupted", exc);
            } finally {
                sockOut.close();
            }
            logger.debug("> Websocket| Done: {}", i);

            future.get();
        } catch (Exception ex) {
            logger.error("Error while initializing websocket connection", ex);
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
            sock.close();
        } catch (IOException ex) {
            logger.debug("Exception while closing socket", ex);
        }
        logger.debug("* Websocket| Upgrade close");
    }
}