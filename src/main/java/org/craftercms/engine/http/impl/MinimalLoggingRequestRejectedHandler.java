/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.http.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import java.io.IOException;

/**
 * {@link RequestRejectedHandler} implementation that logs the exception with a minimal message and stack trace when debug is enabled, and only the message when debug is disabled.
 * It also allows configuring the HTTP status code to be sent in the response.
 */
public class MinimalLoggingRequestRejectedHandler implements RequestRejectedHandler {
	private static final Logger logger = LoggerFactory.getLogger(MinimalLoggingRequestRejectedHandler.class);

	private final int httpError;

	/**
	 * Constructs an instance which uses {@code 400} as response code.
	 */
	public MinimalLoggingRequestRejectedHandler() {
		this(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * Constructs an instance which uses a configurable http code as response.
	 *
	 * @param httpError http status code to use
	 */
	public MinimalLoggingRequestRejectedHandler(int httpError) {
		this.httpError = httpError;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, RequestRejectedException e) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Rejecting request due to: {}", e.getMessage(), e);
		} else {
			logger.info("Rejecting request due to: {}", e.getMessage());
		}
		response.sendError(this.httpError);
	}
}
