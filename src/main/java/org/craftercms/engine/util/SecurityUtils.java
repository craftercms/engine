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
package org.craftercms.engine.util;

import org.apache.commons.lang3.Strings;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Utility class for security related operations.
 */
public class SecurityUtils {
    public static final String ANONYMOUS_PSEUDO_ROLE = "anonymous";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHENTICATED_PSEUDO_ROLE = "authenticated";
	public static final List<String> AUTHENTICATED_PSEUDO_ROLES_SEARCH_VALUES = List.of(AUTHENTICATED_PSEUDO_ROLE, ROLE_PREFIX + AUTHENTICATED_PSEUDO_ROLE);
	public static final List<String> ANONYMOUS_PSEUDO_ROLES_SEARCH_VALUES = List.of(ANONYMOUS_PSEUDO_ROLE, ROLE_PREFIX + ANONYMOUS_PSEUDO_ROLE);
	public static final String KEYWORD_SUFFIX = ".keyword";

    private SecurityUtils() {
    }

	/**
	 * Returns a list of values to be used for authorized roles matching. <br />
	 * For each role, this method will include the role itself and the role with the ROLE_ prefix.
	 *
	 * @param authorities the user authorities/roles
	 * @return a list of authorized roles for matching
	 */
	public static List<String> getAuthorizedRolesMatchValue(@NonNull final Collection<? extends GrantedAuthority> authorities) {
		return authorities.stream()
				.map(GrantedAuthority::getAuthority)
				.flatMap(role -> {
					if (startsWith(role, ROLE_PREFIX)) {
						return Arrays.asList(role, removeStart(role, ROLE_PREFIX)).stream();
					} else {
						return Arrays.asList(role, prependIfMissing(role, ROLE_PREFIX)).stream();
					}
				})
				.toList();
	}

	/**
	 * Returns the role field name ensuring it ends with ".keyword"
	 *
	 * @return the role field name with ".keyword" suffix for search exact matching
	 */
	public static String getRoleFieldNameWithKeyword(String roleFieldName) {
		return Strings.CS.appendIfMissing(roleFieldName, KEYWORD_SUFFIX);
	}

	/**
     * Validates that the user has access to a content protected by the specified roles.
     * This method will throw an {@link AccessDeniedException} if the user doesn't have access.
     * Access is granted if:
     * <ul>
     *     <li>authorizedRoles is empty</li>
     *     <li>authorizedRoles contains 'anonymous' pseudo-role</li>
     *     <li>authorizedRoles contains 'authenticated' pseudo-role and authentication is not anonymous</li>
     *     <li>authorizedRoles contains any of the user roles/authorities</li>
     * </ul>
     */
    public static void checkAccess(Collection<String> authorizedRoles, String url) throws AccessDeniedException, AuthenticationException {
        Authentication authentication = null;

        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            authentication = context.getAuthentication();
        }

        if (isEmpty(authorizedRoles) || containsRole(ANONYMOUS_PSEUDO_ROLE, authorizedRoles)) {
            return;
        }
        // If auth == null it is anonymous
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException(format("User is anonymous but page '%s' requires authentication", url));
        }
        if (!containsRole(AUTHENTICATED_PSEUDO_ROLE, authorizedRoles) && !hasAnyRole(authentication, authorizedRoles)) {
            throw new AccessDeniedException(format("User '%s' is not authorized " +
                    "to view page '%s'", authentication.getName(), url));
        }
    }

    protected static boolean containsRole(String role, Collection<String> roles) {
        return roles.stream()
                .map(r -> removeStart(r, ROLE_PREFIX))
                .anyMatch(r -> r.equalsIgnoreCase(role));
    }

    protected static boolean hasAnyRole(Authentication auth, Collection<String> roles) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> removeStart(authority, ROLE_PREFIX))
                .anyMatch(authority -> containsRole(authority, roles));
    }

}
