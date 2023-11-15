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
package org.craftercms.engine.util.spring.security.preview;

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.engine.exception.HttpStatusCodeException;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.io.IOException;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Filter that checks if the user is authorized to preview the site.
 */
public class PreviewAccessTokenFilter extends GenericFilterBean {
    private final static String PREVIEW_SITE_COOKIE_NAME = "crafterPreview";

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

        String previewCookie = HttpUtils.getCookieValue(PREVIEW_SITE_COOKIE_NAME, httpServletRequest);
        if (isEmpty(previewCookie)) {
            String message = format("User is not authorized to preview site. '%s' cookie not found", PREVIEW_SITE_COOKIE_NAME);
            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, message);
        }

        String[] tokens = decryptCookie(previewCookie);
        if (tokens.length != 2) {
            String message = format("Failed to validate preview site token. Found '%s' elements but expecting 2", PREVIEW_SITE_COOKIE_NAME);
            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, message);
        }

        long tokenTimestamp = Long.parseLong(tokens[1]);
        boolean isExpired = tokenTimestamp < System.currentTimeMillis();
        if (isExpired) {
            String message = format("User is not authorized to preview site '%s', '%s' cookie has expired", site, PREVIEW_SITE_COOKIE_NAME);
            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.FORBIDDEN, message);
        }

        String previewSiteFromCookie = tokens[0];
        if (!site.equals(previewSiteFromCookie)) {
            String message = format("User is not authorized to preview site '%s', '%s' cookie does not match", site, PREVIEW_SITE_COOKIE_NAME);
            logger.error(message);
            throw new HttpStatusCodeException(HttpStatus.FORBIDDEN, message);
        }

        chain.doFilter(request, response);
    }

    /**
     * Decrypts the preview site cookie.
     *
     * @param encryptedCookie the encrypted cookie
     * @return the decrypted cookie as an array of tokens (siteName, expirationTimestamp)
     */
    private String[] decryptCookie(final String encryptedCookie) {
        try {
            return textEncryptor.decrypt(encryptedCookie)
                    .split("\\|");
        } catch (CryptoException e) {
            String message = "Failed to decrypt preview site token";
            logger.error(message, e);
            throw new HttpStatusCodeException(HttpStatus.UNAUTHORIZED, message);
        }
    }
}
