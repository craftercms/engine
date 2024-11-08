/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.engine.exception.HttpStatusCodeException;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Filter that checks if the user is authorized to preview the site.
 */
public class PreviewAccessTokenFilter extends GenericFilterBean {
    private final static String PREVIEW_SITE_TOKEN_NAME = "crafterPreview";
    private final static String PREVIEW_SITE_TOKEN_HEADER_NAME = "X-Crafter-Preview";

    private final TextEncryptor textEncryptor;

    @ConstructorProperties({"textEncryptor"})
    public PreviewAccessTokenFilter(final TextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String site = SiteContext.getCurrent().getSiteName();
        if (isEmpty(site)) {
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
            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, message);
        }

        String[] tokens = decryptPreviewToken(previewToken);
        if (tokens.length != 2) {
            String message = format("Failed to validate preview site token. Found '%s' header or '%s' token elements but expecting 2",
                    PREVIEW_SITE_TOKEN_HEADER_NAME, PREVIEW_SITE_TOKEN_NAME);
            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, message);
        }

        long tokenTimestamp = Long.parseLong(tokens[1]);
        boolean isExpired = tokenTimestamp < System.currentTimeMillis();
        if (isExpired) {
            String message = format("User is not authorized to preview site '%s', '%s' header or '%s' token has expired",
                    site, PREVIEW_SITE_TOKEN_HEADER_NAME, PREVIEW_SITE_TOKEN_NAME);
            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.FORBIDDEN, message);
        }

        String previewSitesFromToken = tokens[0];
        List<String> allowedSites = Arrays.asList(previewSitesFromToken.split(","));
        if (!allowedSites.contains(site)) {
            String message = format("User is not authorized to preview site '%s', '%s' header or '%s' token does not match",
                    site, PREVIEW_SITE_TOKEN_HEADER_NAME, PREVIEW_SITE_TOKEN_NAME);
            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.FORBIDDEN, message);
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
            logger.error(message, e);
            throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, message);
        }
    }
}
