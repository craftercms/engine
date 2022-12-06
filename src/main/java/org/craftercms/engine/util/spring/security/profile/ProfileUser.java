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

package org.craftercms.engine.util.spring.security.profile;

import java.util.Map;
import java.util.Objects;

import org.craftercms.engine.util.spring.security.CustomUser;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static java.util.stream.Collectors.toSet;

/**
 * Extension of {@link CustomUser} to wrap an {@link Authentication} or {@link Profile} instance
 *
 * @author joseross
 * @since 3.1.5
 */
public class ProfileUser extends CustomUser {

    protected Authentication authentication;

    protected Profile profile;

    public ProfileUser(final Authentication auth) {
        this(auth.getProfile());
        this.authentication = auth;
    }

    public ProfileUser(final Profile profile) {
        super(profile.getUsername(), "N/A", profile.isEnabled(), true, true, true,
            profile.getRoles().stream().map(SimpleGrantedAuthority::new).collect(toSet()));
        this.profile = profile;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    public <T> T getAttribute(final String name) {
        return profile.getAttribute(name);
    }

    @Override
    public void setAttributes(final Map<String, Object> attributes) {
        profile.setAttributes(attributes);
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        profile.setAttribute(name, value);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return profile.getAttributes();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfileUser)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ProfileUser details = (ProfileUser)o;
        return Objects.equals(profile, details.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), profile);
    }

}
