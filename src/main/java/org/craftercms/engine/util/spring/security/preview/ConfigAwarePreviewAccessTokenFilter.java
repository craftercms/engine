/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.engine.util.spring.security.preview;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.engine.exception.PreviewAccessException;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.spring.cors.SiteAwareCorsConfigurationSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Filter that checks if the user is authorized to preview the site.
 */
public class ConfigAwarePreviewAccessTokenFilter extends GenericFilterBean {
    private final static String PREVIEW_SITE_TOKEN_NAME = "crafterPreview";
    private final static String PREVIEW_SITE_TOKEN_HEADER_NAME = "X-Crafter-Preview";

    private final TextEncryptor textEncryptor;
	private final SiteAwareCorsConfigurationSource corsConfigSource;

    @ConstructorProperties({"textEncryptor", "corsConfigSource"})
    public ConfigAwarePreviewAccessTokenFilter(final TextEncryptor textEncryptor,
											   final SiteAwareCorsConfigurationSource corsConfigSource) {
        this.textEncryptor = textEncryptor;
		this.corsConfigSource = corsConfigSource;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String site = SiteContext.getCurrent().getSiteName();
        if (isEmpty(site)) {
            chain.doFilter(request, response);
            return;
        }

		if (skipTokenValidation(httpServletRequest)) {
			chain.doFilter(request, response);
			return;
		}

        String previewToken = httpServletRequest.getHeader(PREVIEW_SITE_TOKEN_HEADER_NAME);
        if (isEmpty(previewToken)) {
            previewToken = httpServletRequest.getParameter(PREVIEW_SITE_TOKEN_NAME);
        }
        if (isEmpty(previewToken)) {
            previewToken = HttpUtils.getCookieValue(PREVIEW_SITE_TOKEN_NAME, httpServletRequest);
        }

        if (isEmpty(previewToken)) {
            String message = format("User is not authorized to preview site. '%s' header or '%s' token not found",
                    PREVIEW_SITE_TOKEN_HEADER_NAME, PREVIEW_SITE_TOKEN_NAME);
            logger.debug(message);
            throw new PreviewAccessException(HttpStatus.UNAUTHORIZED, message);
        }

        String[] tokens = decryptPreviewToken(previewToken);
        if (tokens.length != 2) {
            String message = format("Failed to validate preview site token. Found '%s' header or '%s' token elements but expecting 2",
                    PREVIEW_SITE_TOKEN_HEADER_NAME, PREVIEW_SITE_TOKEN_NAME);
            logger.debug(message);
            throw new PreviewAccessException(HttpStatus.UNAUTHORIZED, message);
        }

        long tokenTimestamp = Long.parseLong(tokens[1]);
        boolean isExpired = tokenTimestamp < System.currentTimeMillis();
        if (isExpired) {
            String message = format("User is not authorized to preview site '%s', '%s' header or '%s' token has expired",
                    site, PREVIEW_SITE_TOKEN_HEADER_NAME, PREVIEW_SITE_TOKEN_NAME);
            logger.debug(message);
            throw new PreviewAccessException(HttpStatus.FORBIDDEN, message);
        }

        String previewSitesFromToken = tokens[0];
        List<String> allowedSites = Arrays.asList(previewSitesFromToken.split(","));
        if (!allowedSites.contains(site)) {
            String message = format("User is not authorized to preview site '%s', '%s' header or '%s' token does not match",
                    site, PREVIEW_SITE_TOKEN_HEADER_NAME, PREVIEW_SITE_TOKEN_NAME);
            logger.debug(message);
            throw new PreviewAccessException(HttpStatus.FORBIDDEN, message);
        }

        chain.doFilter(request, response);
    }

    /**
     * Decrypts the preview site token.
     *
     * @param encryptedToken the encrypted token
     * @return the decrypted token as an array of tokens (siteNames, expirationTimestamp)
     */
    private String[] decryptPreviewToken(final String encryptedToken) {
        try {
            return textEncryptor.decrypt(encryptedToken)
                    .split("\\|");
        } catch (CryptoException e) {
            String message = "Failed to decrypt preview site token";
            logger.debug(message, e);
            throw new PreviewAccessException(HttpStatus.UNAUTHORIZED, message);
        }
    }

	/**
	 * Determines whether token validation should be skipped for the given HTTP request.
	 * This is typically used for preflight OPTIONS requests in CORS (Cross-Origin Resource Sharing)
	 * scenarios, where token validation is not required.
	 *
	 * @param request The HTTP request to evaluate.
	 * @return {@code true} if token validation should be skipped, {@code false} otherwise.
	 */
	private boolean skipTokenValidation(HttpServletRequest request) {
		if (!CorsUtils.isCorsRequest(request) || !CorsUtils.isPreFlightRequest(request)) {
			return false;
		}

		return corsAllowedOrigin(request);
	}

	/**
	 * Checks if the `Origin` header of the given HTTP request is allowed based on the
	 * configured CORS (Cross-Origin Resource Sharing) origin patterns. If no CORS configuration
	 * is found or if the `Origin` header is missing or empty, the origin is not allowed.
	 *
	 * @param request The HTTP request to check for allowed CORS origin.
	 * @return {@code true} if the `Origin` header is allowed according to the CORS configuration;
	 *         {@code false} otherwise.
	 */
	private boolean corsAllowedOrigin(HttpServletRequest request) {
		CorsConfiguration corsConfiguration = corsConfigSource.getCorsConfiguration(request);
		if (corsConfiguration == null) {
			return false;
		}

		String origin = request.getHeader("Origin");
		if (isEmpty(origin)) {
			return false;
		}

		return corsConfiguration.checkOrigin(origin) != null;
	}
}
