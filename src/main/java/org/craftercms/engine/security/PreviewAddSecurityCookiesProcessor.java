package org.craftercms.engine.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.security.authentication.Authentication;
import org.craftercms.security.processors.impl.AddSecurityCookiesProcessor;
import org.craftercms.security.utils.SecurityUtils;

/**
 * Extension of {@link org.craftercms.security.processors.impl.AddSecurityCookiesProcessor} that avoids adding
 * the cookies if the authentication is a {@link org.craftercms.engine.security.PersonaAuthentication}.
 *
 * @author avasquez
 */
public class PreviewAddSecurityCookiesProcessor extends AddSecurityCookiesProcessor {

    @Override
    protected AddSecurityCookiesResponseWrapper wrapResponse(RequestContext context) {
        return new PreviewAddSecurityCookiesResponseWrapper(context.getRequest(), context.getResponse());
    }

    protected class PreviewAddSecurityCookiesResponseWrapper extends AddSecurityCookiesResponseWrapper {

        public PreviewAddSecurityCookiesResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        @Override
        public void addCookies() {
            Authentication auth = SecurityUtils.getAuthentication(request);
            if (auth instanceof PersonaAuthentication) {
                // Delete cookies if they still exist from a previous authentication
                deleteTicketCookie();
                deleteProfileLastModifiedCookie();
            } else {
                super.addCookies();
            }
        }
    }

}
